package com.example.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

public class ChecklistDetailPage {

    @AndroidFindBy(accessibility = "checklistDetailTitle")
    @iOSXCUITFindBy(iOSNsPredicate = "identifier == 'checklistDetailTitle'")
    private WebElement checklistDetailTitle;

    @AndroidFindBy(accessibility = "addChecklistItemInput")
    @iOSXCUITFindBy(accessibility = "addChecklistItemInput")
    private WebElement addChecklistItemInput;

    @AndroidFindBy(accessibility = "addChecklistItemButton")
    @iOSXCUITFindBy(accessibility = "addChecklistItemButton")
    private WebElement addChecklistItemButton;

    @AndroidFindBy(accessibility = "checklistBackButton")
    @iOSXCUITFindBy(accessibility = "checklistBackButton")
    private WebElement checklistBackButton;

    public ChecklistDetailPage(RemoteWebDriver driver) {
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ZERO), this);
    }

    public WebElement getChecklistDetailTitle() { return checklistDetailTitle; }
    public WebElement getAddItemInput()          { return addChecklistItemInput; }
    public WebElement getAddItemButton()         { return addChecklistItemButton; }
    public WebElement getBackButton()            { return checklistBackButton; }
}
