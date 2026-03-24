package com.example.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;

public class TimerPage {

    @AndroidFindBy(xpath = "//*[@content-desc='timerModeLabel']")
    @iOSXCUITFindBy(accessibility = "timerModeLabel")
    private WebElement timerModeLabel;

    @AndroidFindBy(xpath = "//*[@content-desc='timerTaskName']")
    @iOSXCUITFindBy(accessibility = "timerTaskName")
    private WebElement timerTaskName;

    @AndroidFindBy(xpath = "//*[@content-desc='timerProgress']")
    @iOSXCUITFindBy(accessibility = "timerProgress")
    private WebElement timerProgress;

    @AndroidFindBy(accessibility = "timerAnimation")
    @iOSXCUITFindBy(accessibility = "timerAnimation")
    private WebElement timerAnimation;

    @AndroidFindBy(accessibility = "breakAnimation")
    @iOSXCUITFindBy(accessibility = "breakAnimation")
    private WebElement breakAnimation;

    public TimerPage(RemoteWebDriver driver) {
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ZERO), this);
    }

    public WebElement getModeElement()     { return timerModeLabel; }
    public WebElement getTaskElement()     { return timerTaskName; }
    public WebElement getProgressElement() { return timerProgress; }
    public WebElement getTimerAnimation()  { return timerAnimation; }
    public WebElement getBreakAnimation()  { return breakAnimation; }
}
