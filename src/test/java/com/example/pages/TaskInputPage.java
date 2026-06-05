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

    @AndroidFindBy(accessibility = "focusDurationValue")
    @iOSXCUITFindBy(iOSNsPredicate = "identifier == 'focusDurationValue'")
    private WebElement focusDurationValue;

    @AndroidFindBy(accessibility = "breakDurationValue")
    @iOSXCUITFindBy(iOSNsPredicate = "identifier == 'breakDurationValue'")
    private WebElement breakDurationValue;

    private final RemoteWebDriver driver;
    private final String platform = System.getProperty("platform", "android");

    // Fokus-presets i sekunder
    private static final int[] FOCUS_PRESETS = {60, 120, 300};
    // Paus-presets i sekunder
    private static final int[] BREAK_PRESETS = {0, 30, 60};

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
                    String actual = field.getAttribute("value");
                    if (actual == null) actual = "";
                    return text.equals(actual) || actual.startsWith(text);
                }
                return true;
            });
    }

    private boolean isPreset(int seconds, int[] presets) {
        for (int p : presets) if (p == seconds) return true;
        return false;
    }

    public void enterTaskName(String task) {
        setFieldText(AppiumBy.accessibilityId("taskInput"), task);
    }

    public void addSubtask(String subtask) {
        setFieldText(AppiumBy.accessibilityId("subtaskInput"), subtask);
        addSubtaskButton.click();
    }

    public void setDuration(String seconds) {
        int secs = Integer.parseInt(seconds);
        if (isPreset(secs, FOCUS_PRESETS)) {
            driver.findElement(AppiumBy.accessibilityId("focusPreset_" + secs)).click();
        } else {
            driver.findElement(AppiumBy.accessibilityId("focusCustomChip")).click();
            setFieldText(AppiumBy.accessibilityId("focusCustomInput"), seconds);
            driver.findElement(AppiumBy.accessibilityId("focusCustomSekButton")).click();
        }
    }

    public void setBreakDuration(String seconds) {
        int secs = Integer.parseInt(seconds);
        if (isPreset(secs, BREAK_PRESETS)) {
            driver.findElement(AppiumBy.accessibilityId("breakPreset_" + secs)).click();
        } else {
            driver.findElement(AppiumBy.accessibilityId("breakCustomChip")).click();
            setFieldText(AppiumBy.accessibilityId("breakCustomInput"), seconds);
            driver.findElement(AppiumBy.accessibilityId("breakCustomSekButton")).click();
        }
    }

    public String getDurationText()      { return focusDurationValue.getText(); }
    public String getBreakDurationText() { return breakDurationValue.getText(); }

    public String getTaskInputText() { return taskInput.getText(); }

    public void fillSubtaskInput(String text) {
        setFieldText(AppiumBy.accessibilityId("subtaskInput"), text);
    }

    public void clickAddSubtask() {
        addSubtaskButton.click();
    }

    public By categoryItemLocator(String name) {
        if ("ios".equalsIgnoreCase(platform)) {
            return AppiumBy.accessibilityId(name);
        }
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + name + "\")");
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
