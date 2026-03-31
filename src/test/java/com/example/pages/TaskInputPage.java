package com.example.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TaskInputPage {

    @AndroidFindBy(accessibility = "taskInput")
    @iOSXCUITFindBy(accessibility = "taskInput")
    private WebElement taskInput;

    @AndroidFindBy(accessibility = "subtaskInput")
    @iOSXCUITFindBy(accessibility = "subtaskInput")
    private WebElement subtaskInput;

    @AndroidFindBy(accessibility = "addSubtaskButton")
    @iOSXCUITFindBy(accessibility = "addSubtaskButton")
    private WebElement addSubtaskButton;

    @AndroidFindBy(accessibility = "durationInput")
    @iOSXCUITFindBy(accessibility = "durationInput")
    private WebElement durationInput;

    @AndroidFindBy(accessibility = "breakDurationInput")
    @iOSXCUITFindBy(accessibility = "breakDurationInput")
    private WebElement breakDurationInput;

    @AndroidFindBy(accessibility = "startButton")
    @iOSXCUITFindBy(accessibility = "startButton")
    private WebElement startButton;

    @AndroidFindBy(xpath = "//*[@content-desc='taskInputError']")
    @iOSXCUITFindBy(accessibility = "taskInputError")
    private WebElement taskInputError;

    @AndroidFindBy(xpath = "//*[@content-desc='subtaskInputError']")
    @iOSXCUITFindBy(accessibility = "subtaskInputError")
    private WebElement subtaskInputError;

    @AndroidFindBy(uiAutomator = "new UiSelector().descriptionContains(\"subtaskChip_\")")
    @iOSXCUITFindBy(xpath = "//*[contains(@name, 'subtaskChip_')]")
    private List<WebElement> subtaskChips;

    @AndroidFindBy(accessibility = "saveTemplateButton")
    @iOSXCUITFindBy(accessibility = "saveTemplateButton")
    private WebElement saveTemplateButton;

    @AndroidFindBy(accessibility = "chooseTemplateButton")
    @iOSXCUITFindBy(accessibility = "chooseTemplateButton")
    private WebElement chooseTemplateButton;

    @AndroidFindBy(accessibility = "templateDialogClose")
    @iOSXCUITFindBy(accessibility = "templateDialogClose")
    private WebElement templateDialogClose;

    @AndroidFindBy(accessibility = "clearButton")
    @iOSXCUITFindBy(accessibility = "clearButton")
    private WebElement clearButton;

    @AndroidFindBy(accessibility = "categoryButton")
    @iOSXCUITFindBy(accessibility = "categoryButton")
    private WebElement categoryButton;

    @AndroidFindBy(accessibility = "selectedCategoryLabel")
    @iOSXCUITFindBy(accessibility = "selectedCategoryLabel")
    private WebElement selectedCategoryLabel;

    @AndroidFindBy(xpath = "//*[@content-desc='templateItemName']")
    @iOSXCUITFindBy(accessibility = "templateItemName")
    private List<WebElement> templateItems;

    private final RemoteWebDriver driver;
    private final String platform = System.getProperty("platform", "android");

    public TaskInputPage(RemoteWebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ZERO), this);
    }

    private void setFieldText(By locator, String text) {
        new WebDriverWait(driver, Duration.ofSeconds(15))
            .ignoring(StaleElementReferenceException.class)
            .until(d -> {
                WebElement field = d.findElement(locator);
                field.clear();
                field.sendKeys(text);
                if ("ios".equalsIgnoreCase(platform)) {
                    String actual = field.getText();
                    return text.equals(actual) || actual.startsWith(text);
                }
                return true;
            });
    }

    public void enterTaskName(String task) {
        setFieldText(AppiumBy.accessibilityId("taskInput"), task);
    }

    public void addSubtask(String subtask) {
        setFieldText(AppiumBy.accessibilityId("subtaskInput"), subtask);
        addSubtaskButton.click();
    }

    public void setDuration(String seconds) {
        setFieldText(AppiumBy.accessibilityId("durationInput"), seconds);
    }

    public void setBreakDuration(String seconds) {
        setFieldText(AppiumBy.accessibilityId("breakDurationInput"), seconds);
    }

    public String getTaskInputText()      { return taskInput.getText(); }
    public String getDurationText()       { return durationInput.getText(); }
    public String getBreakDurationText()  { return breakDurationInput.getText(); }

    public void fillSubtaskInput(String text) {
        setFieldText(AppiumBy.accessibilityId("subtaskInput"), text);
    }

    public void clickAddSubtask() {
        addSubtaskButton.click();
    }

    public WebElement getCategoryButton()        { return categoryButton; }
    public WebElement getSelectedCategoryLabel() { return selectedCategoryLabel; }
    public List<WebElement> getSubtaskChips()        { return subtaskChips; }
    public WebElement getSaveTemplateButton()        { return saveTemplateButton; }
    public WebElement getChooseTemplateButton()      { return chooseTemplateButton; }
    public WebElement getTemplateDialogClose()       { return templateDialogClose; }
    public WebElement getClearButton()               { return clearButton; }
    public List<WebElement> getTemplateItems()       { return templateItems; }
    public boolean isTaskInputErrorVisible()         { return taskInputError.isDisplayed(); }
    public boolean isSubtaskInputErrorVisible()      { return subtaskInputError.isDisplayed(); }

    public void clickStart() {
        startButton.click();
    }
}
