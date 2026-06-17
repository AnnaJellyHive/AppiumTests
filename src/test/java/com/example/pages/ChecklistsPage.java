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

public class ChecklistsPage {

    @AndroidFindBy(accessibility = "createListButton")
    @iOSXCUITFindBy(accessibility = "createListButton")
    private WebElement createListButton;

    @AndroidFindBy(accessibility = "checklistNameInput")
    @iOSXCUITFindBy(accessibility = "checklistNameInput")
    private WebElement checklistNameInput;

    @AndroidFindBy(accessibility = "createListConfirmButton")
    @iOSXCUITFindBy(accessibility = "createListConfirmButton")
    private WebElement createListConfirmButton;

    @AndroidFindBy(accessibility = "cancelCreateListButton")
    @iOSXCUITFindBy(accessibility = "cancelCreateListButton")
    private WebElement cancelCreateListButton;

    @AndroidFindBy(accessibility = "emptyChecklistsText")
    @iOSXCUITFindBy(iOSNsPredicate = "identifier == 'emptyChecklistsText'")
    private WebElement emptyChecklistsText;

    private final RemoteWebDriver driver;
    private final String platform = System.getProperty("platform", "android");

    public ChecklistsPage(RemoteWebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ZERO), this);
    }

    public WebElement getCreateListButton()       { return createListButton; }
    public WebElement getChecklistNameInput()     { return checklistNameInput; }
    public WebElement getCreateListConfirmButton(){ return createListConfirmButton; }
    public WebElement getCancelCreateListButton() { return cancelCreateListButton; }
    public WebElement getEmptyChecklistsText()    { return emptyChecklistsText; }

    public List<WebElement> getChecklistItemTitles() {
        if ("ios".equalsIgnoreCase(platform)) {
            return driver.findElements(AppiumBy.iOSNsPredicate("identifier == 'checklistItemTitle'"));
        }
        return driver.findElements(AppiumBy.accessibilityId("checklistItemTitle"));
    }
}
