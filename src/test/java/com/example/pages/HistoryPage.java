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

    public HistoryPage(RemoteWebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    public void waitForList() {
        taskHistoryList.isDisplayed();
    }

    public List<WebElement> getTitleElements() {
        return driver.findElements(AppiumBy.accessibilityId("taskItemTitle"));
    }
}
