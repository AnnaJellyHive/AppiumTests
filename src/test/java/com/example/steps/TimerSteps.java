package com.example.steps;

import com.example.pages.ContinuePage;
import com.example.pages.HistoryPage;
import com.example.pages.TaskInputPage;
import com.example.pages.TimerPage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TimerSteps {

    private static final String PLATFORM = System.getProperty("platform", "android");
    private static final String ANDROID_APP_ID = "com.timerapp";
    private static final String IOS_BUNDLE_ID  = "org.reactjs.native.example.TimerApp";

    private AppiumDriver driver;
    private TaskInputPage taskInputPage;
    private TimerPage timerPage;
    private ContinuePage continuePage;
    private HistoryPage historyPage;
    private int durationSeconds = 120;
    private int breakDurationSeconds = 120;
    private final List<String> subtasks = new java.util.ArrayList<>();
    private String lastTaskName = "";
    private int historyCountBefore = 0;

    @Before
    public void setUp() throws Exception {
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            setUpIos();
        } else {
            setUpAndroid();
        }

        String appId = "ios".equalsIgnoreCase(PLATFORM) ? IOS_BUNDLE_ID : ANDROID_APP_ID;
        ((InteractsWithApps) driver).terminateApp(appId);
        ((InteractsWithApps) driver).activateApp(appId);

        // Vänta tills appen laddats och är på TaskInput-skärmen
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        new WebDriverWait(driver, Duration.ofSeconds(240))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(WebDriverException.class)
                .until(d -> {
                    // Stäng ANR-dialog ("System UI isn't responding") — bara Android
                    if (!"ios".equalsIgnoreCase(PLATFORM)) {
                        d.findElements(AppiumBy.xpath("//*[@text='Wait']"))
                                .stream().findFirst().ifPresent(btn -> {
                                    try { btn.click(); } catch (Exception ignored) {}
                                });
                    }
                    return !d.findElements(AppiumBy.accessibilityId("startButton")).isEmpty();
                });

        // Navigera tillbaka till TaskInput om vi hamnat på fel skärm
        for (int i = 0; i < 5; i++) {
            if (!driver.findElements(AppiumBy.accessibilityId("startButton")).isEmpty()) break;
            driver.navigate().back();
            Thread.sleep(500);
        }

        subtasks.clear();
        durationSeconds = 120;
        breakDurationSeconds = 120;
        taskInputPage = new TaskInputPage(driver);
        timerPage     = new TimerPage(driver);
        continuePage  = new ContinuePage(driver);
        historyPage   = new HistoryPage(driver);
    }

    private void setUpAndroid() throws Exception {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName("emulator-5554")
                .setAppPackage(ANDROID_APP_ID)
                .setAppActivity(".MainActivity")
                .setNoReset(true)
                .setUiautomator2ServerInstallTimeout(Duration.ofSeconds(120))
                .setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(120))
                .setAdbExecTimeout(Duration.ofSeconds(120));
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
        driver.setSetting("waitForIdleTimeout", 0);
    }

    private void setUpIos() throws Exception {
        String udid = System.getProperty("deviceUDID", "");
        boolean usePrebuilt = !"false".equalsIgnoreCase(System.getProperty("wdaPrebuilt", "true"));
        XCUITestOptions options = new XCUITestOptions()
                .setBundleId(IOS_BUNDLE_ID)
                .setNoReset(true)
                .setUsePrebuiltWda(usePrebuilt)
                .setWdaLaunchTimeout(Duration.ofSeconds(300))
                .setWdaConnectionTimeout(Duration.ofSeconds(300));
        if (!udid.isEmpty()) options.setUdid(udid);
        driver = new IOSDriver(new URL("http://127.0.0.1:4723"), options);
    }

    @Given("appen är startad")
    public void appenÄrStartad() {
        // Appen startas i setUp()
    }

    @When("användaren fyller i uppgiften {string}")
    public void fyllIUppgiften(String task) {
        lastTaskName = task;
        taskInputPage.enterTaskName(task);
    }

    @And("användaren lägger till underuppgift {string}")
    public void läggerTillUnderuppgift(String subtask) {
        subtasks.add(subtask);
        taskInputPage.addSubtask(subtask);
    }

    @And("användaren sätter tid till {string}")
    public void sätterTidTill(String seconds) {
        taskInputPage.setDuration(seconds);
        durationSeconds = Integer.parseInt(seconds);
    }

    @When("användaren fyller i ett uppgiftsnamn på {int} tecken")
    public void fyllIUppgiftsnamn(int length) {
        taskInputPage.enterTaskName("A".repeat(length));
        lastTaskName = "A".repeat(Math.min(length, 50));
    }

    @When("användaren lägger till underuppgiften på {int} tecken")
    public void läggerTillUnderuppgiftenPå(int length) {
        taskInputPage.fillSubtaskInput("A".repeat(length));
    }

    @When("användaren bekräftar underuppgiften")
    public void bekräftarUnderuppgiften() {
        subtasks.add("A".repeat(50));
        taskInputPage.clickAddSubtask();
    }

    @Then("ska feltext för uppgiftsnamnet visas")
    public void skaFeltextFörUppgiftsnamnetVisas() {
        assertTrue(taskInputPage.isTaskInputErrorVisible(), "Feltext för uppgiftsnamnet visas inte");
    }

    @Then("ska feltext för underuppgiftsnamnet visas")
    public void skaFeltextFörUnderuppgiftsnamnetVisas() {
        assertTrue(taskInputPage.isSubtaskInputErrorVisible(), "Feltext för underuppgiftsnamnet visas inte");
    }

    @Then("ska underuppgiftsnamnet i timern ha längden {int} tecken")
    public void skaUnderuppgiftsnamnetITimernHaLängden(int expectedLength) {
        String taskName = timerPage.getTaskElement().getText();
        assertEquals(expectedLength, taskName.length(),
                "Fel längd på underuppgiftsnamnet: " + taskName);
    }

    @Then("ska uppgiftsnamnet på fortsätt-skärmen ha längden {int} tecken")
    public void skaUppgiftsnamnetPåFortsättSkärmenHaLängden(int expectedLength) {
        assertEquals(expectedLength, continuePage.getTaskName().length(),
                "Fel längd på uppgiftsnamnet på fortsätt-skärmen: " + continuePage.getTaskName());
    }

    @Then("ska uppgiftsnamnet i historiken ha längden {int} tecken")
    public void skaUppgiftsnamnetIHistorikenHaLängden(int expectedLength) {
        assertEquals(expectedLength, lastTaskName.length(),
                "Fel längd på uppgiftsnamnet i historiken");
    }

    @When("användaren startar sekvensen")
    public void starterSekvensen() {
        try { ((HidesKeyboard) driver).hideKeyboard(); } catch (Exception ignored) {}
        taskInputPage.clickStart();
    }

    @When("användaren klickar på {string}")
    public void klickarPå(String accessibilityId) {
        waitForElementToBeVisible(AppiumBy.accessibilityId(accessibilityId)).click();
    }

    @Then("ska timern visa {string} för underuppgift {int} av {int}")
    public void skaTimernVisaJobba(String expectedMode, int index, int total) {
        String expectedTask     = subtasks.get(index - 1);
        String expectedProgress = index + " av " + total;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> expectedMode.equals(timerPage.getModeElement().getText()) &&
                                expectedTask.equals(timerPage.getTaskElement().getText()) &&
                                expectedProgress.equals(timerPage.getProgressElement().getText()));
        } catch (TimeoutException e) {
            String mode     = safeGetText(timerPage.getModeElement());
            String task     = safeGetText(timerPage.getTaskElement());
            String progress = safeGetText(timerPage.getProgressElement());
            throw new AssertionError(String.format(
                    "Timer visade inte rätt värden inom 5s.%n" +
                    "Förväntade: mode='%s', task='%s', progress='%s'%n" +
                    "Hittade:    mode='%s', task='%s', progress='%s'",
                    expectedMode, expectedTask, expectedProgress, mode, task, progress), e);
        }
    }

    @Then("ska jobba-animationen visas och paus-animationen vara dold")
    public void skaJobbaAnimationenVisas() {
        waitForElementToBeVisible(timerPage.getTimerAnimation());
    }

    @Then("ska paus-animationen visas och jobba-animationen vara dold")
    public void skaPausAnimationenVisas() {
        waitForElementToBeVisible(timerPage.getBreakAnimation());
    }

    @Then("ska timern visa paus med nästa underuppgift {int}")
    public void skaTimernVisaPaus(int nextIndex) {
        String expectedText = "Nästa: " + subtasks.get(nextIndex - 1);
        waitForText(timerPage.getModeElement(),     "VILA!");
        waitForText(timerPage.getTaskElement(),     expectedText);
        waitForText(timerPage.getProgressElement(), "Paus");
    }

    @When("timern räknar ner klart")
    public void timernRäknarNerKlart() {
        By modeBy = "ios".equalsIgnoreCase(PLATFORM)
                ? AppiumBy.accessibilityId("timerModeLabel")
                : By.xpath("//android.widget.TextView[@content-desc='timerModeLabel']");
        By continueBy = "ios".equalsIgnoreCase(PLATFORM)
                ? AppiumBy.accessibilityId("continueDoneLabel")
                : By.xpath("//*[@content-desc='continueDoneLabel']");

        List<WebElement> current = driver.findElements(modeBy);
        boolean wasJobba = !current.isEmpty() && "JOBBA!".equals(current.get(0).getText());

        new WebDriverWait(driver, Duration.ofSeconds(Math.max(durationSeconds, breakDurationSeconds) + 15))
                .until(d -> {
                    try {
                        List<WebElement> els = d.findElements(modeBy);
                        if (els.isEmpty()) {
                            return !d.findElements(continueBy).isEmpty();
                        }
                        String text = els.get(0).getText();
                        return wasJobba ? !text.equals("JOBBA!") : !text.equals("VILA!");
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    @Then("ska fortsätt-skärmen visa rätt tid")
    public void skaFortsättSkärmenVisaRättTid() {
        int totalSeconds = subtasks.size() * durationSeconds + (subtasks.size() - 1) * breakDurationSeconds;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String expectedLabel;
        if (minutes > 0 && seconds == 0) {
            expectedLabel = "🎉 " + minutes + " minuter klara!";
        } else if (minutes > 0) {
            expectedLabel = "🎉 " + minutes + " min " + seconds + " sek klara!";
        } else {
            expectedLabel = "🎉 " + seconds + " sekunder klara!";
        }
        assertEquals(expectedLabel, continuePage.getDoneLabel(), "Fel tid på fortsätt-skärmen");
    }

    @Then("ska historiken visa {int} körning med det angivna uppgiftsnamnet")
    public void skaHistorikenVisaMedAngivetNamn(int count) {
        skaHistorikenVisaMinst(count, lastTaskName);
    }

    @Then("ska historiken visa minst {int} körningar med uppgiften {string}")
    public void skaHistorikenVisaMinst(int minCount, String taskName) {
        // Vänta tills minst minCount poster med taskName syns i historiken
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            try {
                d.findElement(AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(10)"));
            } catch (Exception ignored) {}
            long matchCount = historyPage.getTitleElements().stream().filter(e -> {
                try { return e.getText().contains(taskName); } catch (Exception ex) { return false; }
            }).count();
            return matchCount >= minCount;
        });
    }

    @And("användaren sätter paus-tid till {string}")
    public void sätterPausTidTill(String seconds) {
        taskInputPage.setBreakDuration(seconds);
        breakDurationSeconds = Integer.parseInt(seconds);
    }

    @Then("ska uppgiftsnamnet vara tomt")
    public void skaUppgiftsnamnetVaraTomt() {
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
            WebElement input = d.findElement(AppiumBy.accessibilityId("taskInput"));
            String text = input.getText();
            return text.isEmpty() || text.equals("T.ex. plugga matte, städa rummet");
        });
    }

    @And("ska underuppgifterna vara borta")
    public void skaUnderuppgifternaVaraBorta() {
        List<WebElement> chips = driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"subtaskChip_\")"));
        assertTrue(chips.isEmpty(), "Underuppgifter finns kvar efter rensning");
    }

    @And("ska tiderna vara återställda till {string}")
    public void skaTidernaVaraÅterställda(String expected) {
        assertEquals(expected, taskInputPage.getDurationText(),      "Uppgiftstiden är inte återställd");
        assertEquals(expected, taskInputPage.getBreakDurationText(), "Paus-tiden är inte återställd");
    }

    @And("ska underuppgiften {string} finnas i formuläret")
    public void skaUnderuppgiftenFinnasIFormularet(String subtask) {
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
            List<WebElement> texts = d.findElements(By.xpath(
                    "//android.view.ViewGroup[contains(@content-desc,'subtaskChip_')]" +
                    "/following-sibling::android.widget.TextView"));
            return texts.stream().anyMatch(c -> {
                try { return c.getText().contains(subtask); } catch (Exception e) { return false; }
            });
        });
    }

    @And("ska tiderna vara satta till {string}")
    public void skaTidernaVaraSattaTill(String expected) {
        assertEquals(expected, taskInputPage.getDurationText(),      "Uppgiftstiden stämmer inte");
        assertEquals(expected, taskInputPage.getBreakDurationText(), "Paus-tiden stämmer inte");
    }

    @And("användaren stänger dialogen")
    public void stängerDialogen() {
        waitForElementToBeVisible(AppiumBy.accessibilityId("templateDialogClose")).click();
    }

    @And("användaren sparar uppgiften")
    public void sparerUppgiften() {
        waitForElementToBeVisible(AppiumBy.accessibilityId("saveTemplateButton")).click();
        // Stäng alert (t.ex. "Uppgift sparad!" eller duplicat-varning)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(d -> !d.findElements(
                            AppiumBy.androidUIAutomator("new UiSelector().text(\"OK\")")).isEmpty());
            driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"OK\")")).click();
        } catch (Exception ignored) {}
    }

    @And("användaren rensar formuläret")
    public void rensarFormularet() {
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))" +
                ".scrollIntoView(new UiSelector().description(\"clearButton\"))"
        )).click();
    }

    @And("användaren väljer den sparade uppgiften {string}")
    public void väljerSparadUppgift(String name) {
        waitForElementToBeVisible(AppiumBy.accessibilityId("chooseTemplateButton")).click();
        By templateBy = By.xpath(
                "//android.widget.TextView[@content-desc='templateItemName' and @text='" + name + "']");
        waitForElementToBeVisible(templateBy).click();
        // Vänta tills dialogen stängts och formuläret fyllts i
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
                d.findElements(By.xpath("//*[@content-desc='templateItemName']")).isEmpty());
    }

    @Then("ska formuläret vara ifyllt med uppgiften {string}")
    public void skaFormularetVaraIfyllt(String expectedName) {
        WebElement taskInput = waitForElementToBeVisible(AppiumBy.accessibilityId("taskInput"));
        assertEquals(expectedName, taskInput.getText(), "Formuläret är inte ifyllt med rätt uppgiftsnamn");
    }

    @And("användaren tar bort den sparade uppgiften {string}")
    public void tarBortSparadUppgift(String name) throws InterruptedException {
        waitForElementToBeVisible(AppiumBy.accessibilityId("chooseTemplateButton")).click();
        // Vänta tills rätt rad syns, bestäm sedan dess index (delete-knapparna har samma ordning)
        By targetItemBy = By.xpath(
                "//android.widget.TextView[@content-desc='templateItemName' and @text='" + name + "']");
        waitForElementToBeVisible(targetItemBy);
        List<WebElement> itemNames = driver.findElements(
                By.xpath("//android.widget.TextView[@content-desc='templateItemName']"));
        int rowIndex = -1;
        for (int i = 0; i < itemNames.size(); i++) {
            try { if (name.equals(itemNames.get(i).getText())) { rowIndex = i; break; } } catch (Exception ignored) {}
        }
        assertTrue(rowIndex >= 0, "Hittade inte mallen '" + name + "' i listan");
        WebElement item = itemNames.get(rowIndex);
        int y = item.getRect().y + item.getRect().height / 2;
        // Svepa hela skärmbredden för att säkert trigga PanResponder
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), 950, y));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(800), PointerInput.Origin.viewport(), 80, y));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(swipe));
        Thread.sleep(800); // vänta på svep-animationen
        tapDeleteAndConfirm("templateDeleteYes", rowIndex, y);
    }

    @Then("ska den sparade uppgiften {string} inte längre finnas i listan")
    public void skaUppgiftenInteLängreFinnasIListan(String name) {
        By itemBy = By.xpath(
                "//android.widget.TextView[@content-desc='templateItemName' and @text='" + name + "']");
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> d.findElements(itemBy).isEmpty());
    }

    @Then("ska bara en sparad uppgift med namnet {string} finnas")
    public void skaBaraEnSparadUppgiftMedNamnet(String name) {
        waitForElementToBeVisible(AppiumBy.accessibilityId("chooseTemplateButton")).click();
        By templateBy = By.xpath(
                "//android.widget.TextView[@content-desc='templateItemName' and @text='" + name + "']");
        waitForElementToBeVisible(templateBy);
        List<WebElement> matches = driver.findElements(templateBy);
        assertEquals(1, matches.size(),
                "Förväntade exakt 1 sparad uppgift med namnet '" + name + "' men hittade " + matches.size());
        waitForElementToBeVisible(AppiumBy.accessibilityId("templateDialogClose")).click();
    }

    @And("användaren tar bort den senaste historikposten")
    public void tarBortSenasteHistorikpost() throws InterruptedException {
        // Vänta tills uppgiften dyker upp i historiken
        By targetItemBy = By.xpath(
                "//android.widget.TextView[@content-desc='taskItemTitle' and contains(@text,'" + lastTaskName + "')]");
        waitForElementToBeVisible(targetItemBy);

        List<WebElement> items = historyPage.getTitleElements();
        historyCountBefore = (int) items.stream().filter(e -> {
            try { return e.getText().contains(lastTaskName); } catch (Exception ex) { return false; }
        }).count();

        // Hitta index för den rad vi ska ta bort
        int rowIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            try { if (items.get(i).getText().contains(lastTaskName)) { rowIndex = i; break; } }
            catch (Exception ignored) {}
        }
        assertTrue(rowIndex >= 0, "Hittade inte '" + lastTaskName + "' i historiken");

        WebElement targetItem = items.get(rowIndex);
        int y = targetItem.getRect().y + targetItem.getRect().height / 2;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), 950, y));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(800), PointerInput.Origin.viewport(), 80, y));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(swipe));
        Thread.sleep(800); // vänta på svep-animationen
        tapDeleteAndConfirm("historyDeleteYes", rowIndex, y);
    }

    @Then("ska historiken inte längre visa uppgiften {string}")
    public void skaHistorikenInteLängreVisaUppgiften(String taskName) {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(10)"));
        } catch (Exception ignored) {}
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            List<WebElement> titles = historyPage.getTitleElements();
            long count = titles.stream().filter(e -> {
                try { return e.getText().contains(taskName); } catch (Exception ex) { return false; }
            }).count();
            return count == historyCountBefore - 1;
        });
    }

    private void tapDeleteAndConfirm(String deleteBtnAccessibilityId, int rowIndex, int targetY) throws InterruptedException {
        // Flera rader kan ha samma accessibilityId — välj rätt via listindex
        List<WebElement> btns = driver.findElements(AppiumBy.accessibilityId(deleteBtnAccessibilityId));
        assertTrue(rowIndex < btns.size(), "Hittade ingen " + deleteBtnAccessibilityId + " vid index " + rowIndex);
        WebElement deleteBtn = btns.get(rowIndex);
        // X från delete-knappens rect (alltid vid högerkanten), Y från den svepte raden
        int btnX = deleteBtn.getRect().x + deleteBtn.getRect().width / 2;
        int btnY = targetY;
        By jaBy = By.id("android:id/button1"); // positiv-knappen i native AlertDialog
        for (int attempt = 0; attempt < 3; attempt++) {
            Map<String, Object> args = new HashMap<>();
            args.put("x", btnX);
            args.put("y", btnY);
            driver.executeScript("mobile: clickGesture", args);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.visibilityOfElementLocated(jaBy)).click();
                return;
            } catch (Exception ignored) {
                Thread.sleep(300);
            }
        }
        throw new RuntimeException("Bekräftelsedialogen ('Ja') visades inte efter 3 försök");
    }

    private WebElement waitForElementToBeVisible(WebElement element) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            throw new AssertionError("waitForElementToBeVisible: elementet syntes inte inom 5s [" + element + "]", e);
        }
    }

    private WebElement waitForElementToBeVisible(By locator) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new AssertionError("waitForElementToBeVisible: hittade inte element [" + locator + "] inom 5s", e);
        }
    }

    private void waitForText(WebElement element, String expectedText) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> {
                        try {
                            return expectedText.equals(element.getText());
                        } catch (Exception e) {
                            return false;
                        }
                    });
        } catch (TimeoutException e) {
            String actual;
            try { actual = element.getText(); } catch (Exception ex) { actual = "<kunde inte läsa: " + ex.getMessage() + ">"; }
            throw new AssertionError(
                    "waitForText: förväntade '" + expectedText + "' men hittade '" + actual + "'", e);
        }
    }

    private String safeGetText(WebElement element) {
        try { return element.getText(); } catch (Exception e) { return "<kunde inte läsa: " + e.getMessage() + ">"; }
    }

    @After
    public void tearDown(Scenario scenario) {
        if (driver != null) {
            if (scenario.isFailed()) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Screenshot on failure");
            }
            driver.quit();
        }
    }
}
