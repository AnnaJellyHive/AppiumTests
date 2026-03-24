package com.example.pages;

import io.appium.java_client.HasClipboard;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

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

    @AndroidFindBy(accessibility = "taskInputError")
    @iOSXCUITFindBy(accessibility = "taskInputError")
    private WebElement taskInputError;

    @AndroidFindBy(accessibility = "subtaskInputError")
    @iOSXCUITFindBy(accessibility = "subtaskInputError")
    private WebElement subtaskInputError;

    @AndroidFindBy(uiAutomator = "new UiSelector().descriptionContains(\"subtaskChip_\")")
    @iOSXCUITFindBy(xpath = "//*[contains(@name, 'subtaskChip_')]")
    private List<WebElement> subtaskChips;

    private final RemoteWebDriver driver;
    private final boolean isIos;

    public TaskInputPage(RemoteWebDriver driver) {
        this.driver = driver;
        this.isIos = driver instanceof IOSDriver;
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    // On iOS, sendKeys triggers autocorrect even with autoCorrect={false} in the app.
    // Clipboard paste bypasses the keyboard entirely, avoiding autocorrect.
    private void setFieldText(WebElement field, String text) {
        if (isIos) {
            ((HasClipboard) driver).setClipboardText(text);
            field.clear();
            field.sendKeys(Keys.chord(Keys.COMMAND, "v"));
        } else {
            field.clear();
            field.sendKeys(text);
        }
    }

    public void enterTaskName(String task) {
        setFieldText(taskInput, task);
    }

    public void addSubtask(String subtask) {
        setFieldText(subtaskInput, subtask);
        addSubtaskButton.click();
    }

    public void setDuration(String seconds) {
        setFieldText(durationInput, seconds);
    }

    public void setBreakDuration(String seconds) {
        setFieldText(breakDurationInput, seconds);
    }

    public String getTaskInputText()      { return taskInput.getText(); }
    public String getDurationText()       { return durationInput.getText(); }
    public String getBreakDurationText()  { return breakDurationInput.getText(); }

    public void fillSubtaskInput(String text) {
        setFieldText(subtaskInput, text);
    }

    public void clickAddSubtask() {
        addSubtaskButton.click();
    }

    public List<WebElement> getSubtaskChips()        { return subtaskChips; }
    public boolean isTaskInputErrorVisible()         { return taskInputError.isDisplayed(); }
    public boolean isSubtaskInputErrorVisible()      { return subtaskInputError.isDisplayed(); }

    public void clickStart() {
        startButton.click();
    }
}
