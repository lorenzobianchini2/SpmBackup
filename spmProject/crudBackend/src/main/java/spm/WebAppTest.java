package spm;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class WebAppTest {
	static String browser;
	static WebDriver driver;
	static String projectPath = System.getProperty("user.dir");

	public static void main(String []args) throws InterruptedException {
		browser = "Chrome";
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\loren\\Desktop\\Nuova cartella (3)\\chromedriver.exe");
		driver = new ChromeDriver();
		testSpmProject();
		driver.close();
	}
	
	public static void testSpmProject() throws InterruptedException {
		driver.get("http://localhost:4200/");
		driver.findElement(By.id("signIn")).click();
		Thread.sleep(5000);
		
		/*driver.findElement(By.className("CwaK9")).click();
		Thread.sleep(3000);
		driver.findElement(By.name("password")).sendKeys("test");
		driver.findElement(By.className("CwaK9")).click();
		
		String at = driver.getTitle();
		String et = "Gmail";
		
		System.out.println(at);
		
		if(at.equals(et)) {
			System.out.println("test failure");
		} else System.out.println("test successful");
		Thread.sleep(10000);*/
		return;
	}
}
