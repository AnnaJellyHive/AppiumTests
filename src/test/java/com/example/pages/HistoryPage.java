package com.example.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;
import java.util.List;

public class HistoryPage {

    @AndroidFindBy(accessibility = "taskHistoryList")
    @iOSXCUITFindBy(accessibility = "taskHistoryList")
    private WebElement taskHistoryList;

    private final RemoteWebDriver driver;
    private final String platform = System.getProperty("platform", "android");

    public HistoryPage(RemoteWebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    public void waitForList() {
        taskHistoryList.isDisplayed();
    }

    public WebElement getTaskHistoryList() { return taskHistoryList; }

    public List<WebElement> getTitleElements() {
        if ("ios".equalsIgnoreCase(platform)) {
            return driver.findElements(AppiumBy.iOSNsPredicateString("identifier == 'taskItemTitle'"));
        }
        return driver.findElements(AppiumBy.accessibilityId("taskItemTitle"));
    }
}
