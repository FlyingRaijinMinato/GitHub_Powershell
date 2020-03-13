import java.util.Scanner;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.*; 
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

class Script{

    static Actions actions;
    static WebDriver driver;

    Script(){}

    public static void create(String username_or_email, String password, String repository_name, String description) {
       System.setProperty("webdriver.chrome.driver", "C:\\chromedriver_win32\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NONE);
        options.addArguments("start-maximized"); 
        options.addArguments("enable-automation");
        options.addArguments("--no-sandbox"); 
        options.addArguments("--disable-infobars"); 
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation"); 
        options.addArguments("--disable-gpu"); 
        
        try{
            driver = new ChromeDriver(options);
            driver.get("https://github.com/login");
            
            actions = new Actions(driver);

            waitForLoad(driver);

            login(username_or_email, password);

            waitForLoad(driver);

            check_for_verification();

            waitForLoad(driver);

            driver.get("https://github.com/new");

            waitForLoad(driver);

            create_repository(repository_name, description);

            waitForLoad(driver);

            copy_remote();

            Thread.sleep(1500);
            driver.quit();
        }catch(Exception e){
            driver.quit();
            return;
        }
    }

    private static void login(String username_or_email, String password) throws Exception{
        actions.click(driver.findElement(By.xpath("//*[@id=\"login_field\"]"))).sendKeys(driver.findElement(By.xpath("//*[@id=\"login_field\"]")), username_or_email).build().perform();
        Thread.sleep(600);
        actions.click(driver.findElement(By.xpath("//*[@id=\"password\"]"))).sendKeys(driver.findElement(By.xpath("//*[@id=\"password\"]")), password).build().perform();
        Thread.sleep(600);
        actions.click(driver.findElement(By.xpath("//*[@id=\"login\"]/form/div[4]/input[9]"))).build().perform();
    }

    private static void check_for_verification() throws Exception{
        
            Thread.sleep(600);
            boolean is_present = driver.findElements(By.xpath("//*[@id=\"otp\"]")).size() > 0;

            if(is_present){
                Scanner scanner  = new Scanner(System.in);
                System.out.println("Verification code:");
                String code = scanner.next();

                    actions.click(driver.findElement(By.xpath("//*[@id=\"otp\"]"))).sendKeys(driver.findElement(By.xpath("//*[@id=\"otp\"]")), code).build().perform();
                    actions.click(driver.findElement(By.xpath("/html/body/div[3]/main/div/div[3]/form/button"))).build().perform();

                    scanner.close();
                }
        }

    private static void create_repository(String repository_name, String description) throws Exception{
            actions.click(driver.findElement(By.xpath("//*[@id=\"repository_name\"]"))).sendKeys(driver.findElement(By.xpath("//*[@id=\"repository_name\"]")), repository_name).build().perform();
            Thread.sleep(600);
            actions.click(driver.findElement(By.xpath("//*[@id=\"repository_description\"]"))).sendKeys(driver.findElement(By.xpath("//*[@id=\"repository_description\"]")), description).build().perform();
            Thread.sleep(600);
            actions.click(driver.findElement(By.xpath("//*[@id=\"repository_auto_init\"]"))).build().perform();
            Thread.sleep(600);
            actions.click(driver.findElement(By.xpath("//*[@id=\"new_repository\"]/div[3]/button"))).build().perform();
    }

    private static void copy_remote() throws Exception{
        actions.click(driver.findElement(By.xpath("//*[@id=\"js-repo-pjax-container\"]/div[2]/div/div[3]/details[2]/summary"))).build().perform();
        waitForLoad(driver);
        actions.click(driver.findElement(By.xpath("//*[@id=\"js-repo-pjax-container\"]/div[2]/div/div[3]/details[2]/div/div/div[1]/div[1]/div/input"))).build().perform();
        
            Thread.sleep(1500);

        driver.findElement(By.xpath("//*[@id=\"js-repo-pjax-container\"]/div[2]/div/div[3]/details[2]/div/div/div[1]/div[1]/div/input")).sendKeys(Keys.chord(Keys.CONTROL,"c"));
    }



    private static void waitForLoad(WebDriver driver) throws Exception{
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }
}