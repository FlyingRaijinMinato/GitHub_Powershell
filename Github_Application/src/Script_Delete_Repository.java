import java.util.Scanner;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.*; 
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

class Script_Delete_Repository{

    static Actions actions;
    static WebDriver driver;

    Script_Delete_Repository(){}

    public static void deleteRepository(String username_or_email, String password, String clone_link){
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
            
            String urlSettings = clone_link.substring(0, clone_link.length()-4) + "/settings";

            driver.get(urlSettings);

            waitForLoad(driver);

            Thread.sleep(2000);

            actions.click(driver.findElement(By.xpath("//*[@id=\"options_bucket\"]/div[8]/ul/li[4]/details/summary"))).build().perform();
            
            waitForLoad(driver);

            String inputTextField = clone_link.substring(19, clone_link.length() - 4);
            actions.click(driver.findElement(By.xpath("//*[@id=\"options_bucket\"]/div[8]/ul/li[4]/details/details-dialog/div[3]/form/p/input")))
                    .sendKeys(driver.findElement(By.xpath("//*[@id=\"options_bucket\"]/div[8]/ul/li[4]/details/details-dialog/div[3]/form/p/input")), inputTextField)
                    .build()
                    .perform();
            
            Thread.sleep(1500);

            actions.click(driver.findElement(By.xpath("//*[@id=\"options_bucket\"]/div[8]/ul/li[4]/details/details-dialog/div[3]/form/button")))
                    .build()
                    .perform();
            
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