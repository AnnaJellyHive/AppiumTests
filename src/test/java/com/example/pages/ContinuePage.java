package com.example.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;

public class ContinuePage {

    @AndroidFindBy(accessibility = "continueDoneLabel")
    @iOSXCUITFindBy(accessibility = "continueDoneLabel")
    private WebElement continueDoneLabel;

    @AndroidFindBy(accessibility = "continueTaskName")
    @iOSXCUITFindBy(accessibility = "continueTaskName")
    private WebElement continueTaskName;

    @AndroidFindBy(accessibility = "continueYesButton")
    @iOSXCUITFindBy(accessibility = "continueYesButton")
    private WebElement continueYesButton;

    @AndroidFindBy(accessibility = "continueNoButton")
    @iOSXCUITFindBy(accessibility = "continueNoButton")
    private WebElement continueNoButton;

    public ContinuePage(RemoteWebDriver driver) {
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    public String getDoneLabel()  { return continueDoneLabel.getText(); }
    public String getTaskName()   { return continueTaskName.getText(); }
    public void clickYes() { continueYesButton.click(); }
    public void clickNo()  { continueNoButton.click(); }
}
