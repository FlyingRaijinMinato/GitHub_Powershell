import java.util.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import org.json.*;

class main {

    private static Connection connection = null;
    private static boolean quit = false;
    private static String commands = "*********************************************\n" 
                                   + "Quit: q\n"
                                   + "Get content: dir\n"
                                   + "Create Repository: cr\n"
                                   + "Create Git Project Folder: crf\n" 
                                   + "Delete Repository: del\n" 
                                   + "Update DB with html response: u\n"
                                   + "Wipe DB: w\n"
                                   + "*********************************************";

    private static Scanner scanner = new Scanner(System.in);
    private static String password_hash;
    private static String email_hash;
    private static String email;
    private static String password;
    private static String username;

    public static void main(String[] args) {

        username = args[0];
        email = args[1];
        password = args[2];

        try {
            password_hash = getSHA(password);
            email_hash = getSHA(email);
        } catch (Exception e) {
            System.out.println(e);
        }

        connect();

        do {
            showActions();
        } while (!quit);

        scanner.close();

        try {
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void showActions() {
        System.out.println(commands);
        String action = scanner.next();

        switch (action) {
            case "q":
                quit = true;
                break;

            case "dir":
                update(username);
                showDatabase();
                break;

            case "cr":
                System.out.println("Repository name:");
                String repository_name = scanner.next();
                System.out.println("Description:\n");
                scanner.nextLine();
                String description = scanner.nextLine();
                Script.create(email, password, repository_name, description);
                update(username);
                break;

            case "del":
                System.out.println("Clone Link:");
                String clone = scanner.next();
                Script_Delete_Repository.deleteRepository(email, password, clone);
                update(username);
                break;

            case "u":
                storeResponseContent(update(username));
                break;

            case "w":
                wipe();
                break;
                
            case "crf":
                createProjectFolder();
                break;

            default:
                System.err.println("Wrong input!");
        }
    }

    private static void storeResponseContent(String content){
        System.out.println("Do you want to store the html response? [Y][N]");
                String whatDo = scanner.next();

                    switch(whatDo){
                    case "Y":
                        System.out.println("Directory to store 'json.txt' in:");
                        String dir = scanner.next();
                        dir = dir.replace("\\\\","\\\\\\\\");
                        writeResponse(content, dir);
                        break;

                    case "N":
                        //Do nothing
                        break;

                    default:
                        System.out.println("Wrong input!");
                }
    }
   
    private static void createProjectFolder(){
        System.out.println("Directory to create project in:");
        String dir = scanner.next();
        System.out.println("Select project id:");
        String id = scanner.next();

        String clone = "";
        String name = "";

        String sql = "SELECT name , clone FROM GITHUB WHERE userhash = ? AND projectid = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, AES.encrypt(email_hash, password_hash));
            preparedStatement.setString(2, AES.encrypt(id, password_hash));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                clone = AES.decrypt(resultSet.getString("clone"), password_hash);
                name = AES.decrypt(resultSet.getString("name"), password_hash);
            }

            if(!clone.equals("") && !name.equals("")){
                String command = String.format("powershell.exe GitHub-CreateFolder -NAME %1$s -CLONE %2$s -DIR %3$s", name, clone, dir);
                System.out.println("If interrupted, check Internet connection!");
                Runtime.getRuntime().exec(command).waitFor();

                System.out.println("Do you want to open VSCode? [Y] [N]:");
                switch(scanner.next()){
                    case "Y":
                        openCode(dir+"\\"+name);
                        System.out.println("Exit Java? [Y] [N]:");
                        switch(scanner.next()){
                            case "Y":
                                System.exit(0);
                                break;
                            case "N":
                                break;

                            default:
                                System.out.println("Wrong input!");
                        }
                        break;

                    case "N":
                        break;
                    default:
                        System.out.println("Wrong input!");
                }
            }else{
                System.out.println("Could not fetch values from DB!");
            }


        }catch(Exception e){
            return;
        }
    }

    private static void openCode(String dir){
        String command = String.format("powershell.exe VSCode-Open -DIR %1$s", dir);
                
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            System.out.println("Could not open VS Code!");
            return;
        }
    }

    private static void wipe(){
        System.out.println("ARE YOU SURE YOU WANT TO WIPE? [Y][N]:");
        String whatDo = scanner.next();

        switch (whatDo){
            case "N":
                //Nothing
                break;

            case "Y":
                String sql = "DELETE FROM GITHUB";

                try {
                    PreparedStatement p_stmt = connection.prepareStatement(sql);
                    p_stmt.executeUpdate();
                }catch (Exception e){System.out.println("Could not execute wipe query!"); return;}
                System.out.println("Database successfully wiped!"+"\n");
                break;

            default:
                System.out.println("WRONG INPUT!");
        }
    }

    private static String update(String username) {
        HttpURLConnection conn;
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        try {
            URL url = new URL("https://api.github.com/users/" + username + "/repos");
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();

            if (status > 299 || status < 200) {
                System.out.println("Update Connection unsuccessfull...");
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }

                reader.close();
                return null;

            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }

                reader.close();
                conn.disconnect();

                String sql = "DELETE FROM GITHUB WHERE userhash = ?";

                try {
                    PreparedStatement p_stmt = connection.prepareStatement(sql);
                    p_stmt.setString(1, AES.encrypt(email_hash, password_hash));
                    p_stmt.executeUpdate();
                }catch (Exception e){System.out.println("Could not execute delete query!"); return null;}

                parseAndStore(responseContent.toString());
            }
        } catch (Exception e) {
            System.out.println("Could not establish Update Connection!");
        }

        return responseContent.toString();
    }

    private static void parseAndStore(String responseBody) {
        JSONArray input = new JSONArray(responseBody);
        ArrayList<String> id = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();
        ArrayList<String> clone = new ArrayList<>();
        ArrayList<String> created = new ArrayList<>();
        ArrayList<String> updated = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            JSONObject object = input.getJSONObject(i);
            id.add(""+object.getInt("id"));
            name.add(object.getString("name"));
            status.add(!object.getBoolean("private") ? "public" : "private");
            clone.add(object.getString("clone_url"));
            created.add(object.getString("created_at"));
            updated.add(object.getString("updated_at"));
        }

        for(int i = 0; i < id.size(); i++){
            sqlInsert(id.get(i),name.get(i), status.get(i), clone.get(i), created.get(i), updated.get(i));
        }
    }

    private static void writeResponse(String htmlResponse, String directory){
        String filename = "json.txt";

        File direc = new File(directory);
        File actualFile = new File(direc, filename);

        try{
        FileWriter fw = new FileWriter(actualFile);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(""+htmlResponse);
        bw.close();

        }catch(Exception e){
            System.out.println("Could not write response!");
            return;
        }
        
    }

    private static void sqlInsert(String projId, String name, String status, String clone, String created, String updated){
        String sql = "INSERT INTO GITHUB (userhash, projectid, name, status, clone, updated, created) VALUES(?,?,?,?,?,?,?)";
        

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, AES.encrypt(email_hash, password_hash));
            preparedStatement.setString(2, AES.encrypt(projId, password_hash));
            preparedStatement.setString(3, AES.encrypt(name, password_hash));
            preparedStatement.setString(4, AES.encrypt(status, password_hash));
            preparedStatement.setString(5, AES.encrypt(clone, password_hash));
            preparedStatement.setString(6, AES.encrypt(updated, password_hash));
            preparedStatement.setString(7, AES.encrypt(created, password_hash));

            preparedStatement.executeUpdate();
        }catch(Exception e){
            System.out.println("Could not execute insert query!");
            return;
        }
    }

    private static void showDatabase() {
        String sql0 = "SELECT projectid FROM GITHUB WHERE userhash = ?";
        String sql1 = "SELECT name FROM GITHUB WHERE userhash = ?";
        String sql2 = "SELECT status FROM GITHUB WHERE userhash = ?";
        String sql3 = "SELECT clone FROM GITHUB WHERE userhash = ?";
        String sql4 = "SELECT updated FROM GITHUB WHERE userhash = ?";
        String sql5 = "SELECT created FROM GITHUB WHERE userhash = ?";

        ArrayList<String> projectId = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();
        ArrayList<String> clone = new ArrayList<>();
        ArrayList<String> updated = new ArrayList<>();
        ArrayList<String> created = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(sql0);
            statement.setString(1, AES.encrypt(email_hash, password_hash));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                projectId.add(AES.decrypt(resultSet.getString("projectid"), password_hash));
            }

            statement = connection.prepareStatement(sql1);
            statement.setString(1, AES.encrypt(email_hash, password_hash));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                name.add(AES.decrypt(resultSet.getString("name"), password_hash));
            }

            statement = connection.prepareStatement(sql2);
            statement.setString(1, AES.encrypt(email_hash, password_hash));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                status.add(AES.decrypt(resultSet.getString("status"), password_hash));
            }

            statement = connection.prepareStatement(sql3);
            statement.setString(1, AES.encrypt(email_hash, password_hash));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                clone.add(AES.decrypt(resultSet.getString("clone"), password_hash));
            }

            statement = connection.prepareStatement(sql4);
            statement.setString(1, AES.encrypt(email_hash, password_hash));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                updated.add(AES.decrypt(resultSet.getString("updated"), password_hash));
            }

            statement = connection.prepareStatement(sql5);
            statement.setString(1, AES.encrypt(email_hash, password_hash));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                created.add(AES.decrypt(resultSet.getString("created"), password_hash));
            }

        } catch (Exception e) {
            System.out.println("Unable to fetch values!");
            return;
        }
        System.out.printf("=========================================================================================================================================================================================%n");
        System.out.printf("%-20s ||%-20s ||%-10s ||%-80s ||%-20s ||%-20s%n", "ID", "NAME", "STATUS", "CLONE", "UPDATED", "CREATED");
        System.out.printf("+++++++++++++++++++++||+++++++++++++++++++++||+++++++++++||+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++||+++++++++++++++++++++||++++++++++++++++++++%n");
        if(!(projectId.size() > 0)){
            System.out.printf("%-20s ||%-20s ||%-10s ||%-80s ||%-20s ||%-20s%n", "//EMPTY", "//EMPTY", "//EMPTY", "//EMPTY", "//EMPTY", "//EMPTY");
        }else{
            for(int i = 0; i < projectId.size(); i++){
                print_results(projectId.get(i), name.get(i), status.get(i), clone.get(i), updated.get(i), created.get(i));
            }
        }
        System.out.printf("---------------------||---------------------||-----------||---------------------------------------------------------------------------------||---------------------||--------------------%n");
        System.out.printf("=========================================================================================================================================================================================%n");
    }
    
    private static void print_results(String project_id, String name, String status, String clone, String updated, String created){
        System.out.printf("---------------------||---------------------||-----------||---------------------------------------------------------------------------------||---------------------||--------------------%n");
        System.out.printf("%-20s ||%-20s ||%-10s ||%-80s ||%-20s ||%-20s%n", project_id, name, status, clone, updated, created);
    }

    private static String getSHA(String input) throws NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        byte[] digestedMessage = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));

        BigInteger number = new BigInteger(1, digestedMessage);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    private static void connect() {
        String sql = "CREATE TABLE IF NOT EXISTS GITHUB (\n" + "     userhash TEXT NOT NULL,\n"
                + "     projectid TEXT NOT NULL,\n" + "     name TEXT NOT NULL,\n" + "     status TEXT NOT NULL,\n"
                + "     clone TEXT NOT NULL,\n" + "     created TEXT NOT NULL,\n" + "     updated TEXT NOT NULL,\n"
                + ");";

        try

        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:c:\\sqlite\\db\\github.db");
            System.out.println("SQLite GitHub DB connected!");
        } catch (Exception e) {
            System.err.println(e);
        }

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (Exception exception) {
            System.out.println("You have a storage! What would you like to do?");
        }
    }
}