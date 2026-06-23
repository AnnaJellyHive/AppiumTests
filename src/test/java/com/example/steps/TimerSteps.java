package com.example.steps;

import com.example.pages.ChecklistDetailPage;
import com.example.pages.ChecklistsPage;
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
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TimerSteps {

    private static final String PLATFORM = System.getProperty("platform", "android");
    private static final String ANDROID_APP_ID = "com.timerapp";
    private static final String IOS_BUNDLE_ID  = "com.annamarkstrom.zonat";

    private static AppiumDriver driver;
    private TaskInputPage taskInputPage;
    private TimerPage timerPage;
    private ContinuePage continuePage;
    private HistoryPage historyPage;
    private ChecklistsPage checklistsPage;
    private ChecklistDetailPage checklistDetailPage;
    private int durationSeconds = 120;
    private int breakDurationSeconds = 120;
    private final List<String> subtasks = new java.util.ArrayList<>();
    private String lastTaskName = "";

    @Before
    public void setUp() throws Exception {
        if (driver == null) {
            if ("ios".equalsIgnoreCase(PLATFORM)) {
                setUpIos();
            } else {
                setUpAndroid();
            }
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
        taskInputPage       = new TaskInputPage(driver);
        timerPage           = new TimerPage(driver);
        continuePage        = new ContinuePage(driver);
        historyPage         = new HistoryPage(driver);
        checklistsPage      = new ChecklistsPage(driver);
        checklistDetailPage = new ChecklistDetailPage(driver);
    }

    private void setUpAndroid() throws Exception {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName("emulator-5554")
                .setAppPackage(ANDROID_APP_ID)
                .setAppActivity(".SplashActivity")
                .setNoReset(true)
                .setUiautomator2ServerInstallTimeout(Duration.ofSeconds(120))
                .setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(120))
                .setAdbExecTimeout(Duration.ofSeconds(120));
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
        driver.setSetting("waitForIdleTimeout", 0);
    }

    private void setUpIos() throws Exception {
        String udid = System.getProperty("deviceUDID", "");
        XCUITestOptions options = new XCUITestOptions()
                .setBundleId(IOS_BUNDLE_ID)
                .setNoReset(true)
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
        int expectedCount = subtasks.size();
        taskInputPage.addSubtask(subtask);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(d -> taskInputPage.getSubtaskChips().size() >= expectedCount);
        } catch (TimeoutException e) {
            // Retry once — på Android kan keyboardtiming göra att addSubtaskButton-klicket
            // inte registreras om en React-omrendering sker precis efter sendKeys
            taskInputPage.addSubtask(subtask);
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(d -> taskInputPage.getSubtaskChips().size() >= expectedCount);
        }
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
        By locator = "ios".equalsIgnoreCase(PLATFORM)
            ? AppiumBy.accessibilityId("taskInputError")
            : AppiumBy.androidUIAutomator("new UiSelector().text(\"Max 50 tecken\")");
        waitForElementToBeVisible(locator);
    }

    @Then("ska feltext för underuppgiftsnamnet visas")
    public void skaFeltextFörUnderuppgiftsnamnetVisas() {
        By locator = "ios".equalsIgnoreCase(PLATFORM)
            ? AppiumBy.accessibilityId("subtaskInputError")
            : AppiumBy.androidUIAutomator("new UiSelector().text(\"Max 50 tecken\")");
        waitForElementToBeVisible(locator);
    }

    @Then("ska underuppgiftsnamnet i timern ha längden {int} tecken")
    public void skaUnderuppgiftsnamnetITimernHaLängden(int expectedLength) {
        String taskName = getElementText(timerPage.getTaskElement());
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

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> { taskInputPage.clickStart(); return true; });
        // Vänta tills timer-skärmen är synlig innan steget returnerar.
        // Timern startar via useFocusEffect — på CI kan navigationsanimationen ta länge,
        // och timer-fasen hinner gå ut om vi börjar kolla för tidigt.
        new WebDriverWait(driver, Duration.ofSeconds(60))
                .ignoring(WebDriverException.class)
                .until(d -> !d.findElements(AppiumBy.accessibilityId("cancelTimerButton")).isEmpty());
    }

    @When("användaren klickar på {string}")
    public void klickarPå(String accessibilityId) {
        waitForElementToBeVisible(AppiumBy.accessibilityId(accessibilityId), 15).click();
    }

    @Then("ska timern visa {string} för underuppgift {int} av {int}")
    public void skaTimernVisaJobba(String expectedMode, int index, int total) {
        String expectedTask     = subtasks.get(index - 1);
        String expectedProgress = index + " av " + total;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .ignoring(WebDriverException.class)
                    .until(d -> expectedMode.equals(getElementText(timerPage.getModeElement())) &&
                                expectedTask.equals(getElementText(timerPage.getTaskElement())) &&
                                expectedProgress.equals(getElementText(timerPage.getProgressElement())));
        } catch (TimeoutException e) {
            String mode     = safeGetText(timerPage.getModeElement());
            String task     = safeGetText(timerPage.getTaskElement());
            String progress = safeGetText(timerPage.getProgressElement());
            throw new AssertionError(String.format(
                    "Timer visade inte rätt värden inom 20s.%n" +
                    "Förväntade: mode='%s', task='%s', progress='%s'%n" +
                    "Hittade:    mode='%s', task='%s', progress='%s'",
                    expectedMode, expectedTask, expectedProgress, mode, task, progress));
        }
    }

    @Then("ska jobba-animationen visas och paus-animationen vara dold")
    public void skaJobbaAnimationenVisas() {
        waitForElementToBeVisible(timerPage.getTimerAnimation());
        waitForTextIgnoringWDE(timerPage.getModeElement(), "FOKUS!");
    }

    @Then("ska paus-animationen visas och jobba-animationen vara dold")
    public void skaPausAnimationenVisas() {
        waitForElementToBeVisible(timerPage.getTimerAnimation());
        waitForTextIgnoringWDE(timerPage.getModeElement(), "Ta en paus");
    }

    @Then("ska timern visa paus med nästa underuppgift {int}")
    public void skaTimernVisaPaus(int nextIndex) {
        String expectedText = "Nästa: " + subtasks.get(nextIndex - 1);
        waitForTextIgnoringWDE(timerPage.getModeElement(),     "Ta en paus");
        waitForTextIgnoringWDE(timerPage.getTaskElement(),     expectedText);
        waitForTextIgnoringWDE(timerPage.getProgressElement(), "Paus");
    }

    @When("timern räknar ner klart")
    public void timernRäknarNerKlart() {
        By modeBy = AppiumBy.accessibilityId("timerModeLabel");
        By continueBy = AppiumBy.accessibilityId("continueDoneLabel");

        List<WebElement> current = driver.findElements(modeBy);
        boolean wasFokus = !current.isEmpty() && "FOKUS!".equals(getElementText(current.get(0)));

        new WebDriverWait(driver, Duration.ofSeconds(Math.max(durationSeconds, breakDurationSeconds) + 15))
                .until(d -> {
                    try {
                        List<WebElement> els = d.findElements(modeBy);
                        if (els.isEmpty()) {
                            return !d.findElements(continueBy).isEmpty();
                        }
                        String text = getElementText(els.get(0));
                        return wasFokus ? !text.equals("FOKUS!") : !text.equals("Ta en paus");
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    @Then("ska fortsätt-skärmen visa rätt tid")
    public void skaFortsättSkärmenVisaRättTid() {
        waitForElementToBeVisible(AppiumBy.accessibilityId("continueDoneLabel"));
        int totalSeconds = subtasks.size() * durationSeconds + (subtasks.size() - 1) * breakDurationSeconds;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String expectedLabel;
        if (minutes > 0 && seconds == 0) {
            expectedLabel = minutes + " min";
        } else if (minutes > 0) {
            expectedLabel = minutes + " min " + seconds + " sek";
        } else {
            expectedLabel = seconds + " sek";
        }
        assertEquals(expectedLabel, continuePage.getTimeLabel(), "Fel tid på fortsätt-skärmen");
    }

    @Then("ska historiken visa {int} körning med det angivna uppgiftsnamnet")
    public void skaHistorikenVisaMedAngivetNamn(int count) {
        skaHistorikenVisaMinst(count, lastTaskName);
    }

    @Then("ska historiken visa minst {int} körningar med uppgiften {string}")
    public void skaHistorikenVisaMinst(int minCount, String taskName) {
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(d ->
                d.findElements(AppiumBy.iOSNsPredicateString(
                    "label CONTAINS '" + taskName + "'")).size() >= minCount);
        } else {
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
                List<WebElement> titles = historyPage.getTitleElements();
                if (titles.size() < minCount) return false;
                return titles.subList(0, minCount).stream().allMatch(e -> {
                    try { return e.getText().contains(taskName); } catch (Exception ex) { return false; }
                });
            });
        }
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
            return text.isEmpty() || text.equals("T.ex. städa rummet, läsa bok");
        });
    }

    @And("ska underuppgifterna vara borta")
    public void skaUnderuppgifternaVaraBorta() {
        List<WebElement> chips = taskInputPage.getSubtaskChips();
        assertTrue(chips.isEmpty(), "Underuppgifter finns kvar efter rensning");
    }

    @And("ska tiderna vara återställda till fokus {string} och paus {string}")
    public void skaTidernaVaraÅterställda(String expectedFocus, String expectedBreak) {
        assertEquals(expectedFocus, taskInputPage.getDurationText(),      "Fokustiden är inte återställd");
        assertEquals(expectedBreak, taskInputPage.getBreakDurationText(), "Paus-tiden är inte återställd");
    }

    @And("ska underuppgiften {string} finnas i formuläret")
    public void skaUnderuppgiftenFinnasIFormularet(String subtask) {
        // getSubtaskChips() returnerar chip-vyn vars getText() ger accessibilityLabel ("subtaskChip_0"),
        // inte text-innehållet. Sök direkt på texten i det inre text-elementet istället.
        By locator = "ios".equalsIgnoreCase(PLATFORM)
            ? By.xpath("//XCUIElementTypeStaticText[contains(@label, '" + subtask + "')]")
            : AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + subtask + "\")");
        waitForElementToBeVisible(locator);
    }

    @And("ska tiderna vara satta till {string}")
    public void skaTidernaVaraSattaTill(String expected) {
        assertEquals(expected, taskInputPage.getDurationText(),      "Uppgiftstiden stämmer inte");
        assertEquals(expected, taskInputPage.getBreakDurationText(), "Paus-tiden stämmer inte");
    }

    @And("användaren stänger dialogen")
    public void stängerDialogen() {
        waitForElementToBeVisible(taskInputPage.getTemplateDialogClose()).click();
    }

    @And("användaren sparar uppgiften")
    public void sparerUppgiften() {
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            try { driver.executeScript("mobile: hideKeyboard"); } catch (Exception ignored) {}
            driver.executeScript("mobile: scroll",
                    Map.of("direction", "down", "name", "saveTemplateButton"));
        }
        waitForElementToBeVisible(taskInputPage.getSaveTemplateButton(), 15).click();
        try {
            if ("ios".equalsIgnoreCase(PLATFORM)) {
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(d -> { try { d.switchTo().alert(); return true; } catch (Exception e) { return false; } });
                driver.switchTo().alert().accept();
            } else {
                new WebDriverWait(driver, Duration.ofSeconds(15))
                        .until(d -> !d.findElements(AppiumBy.androidUIAutomator("new UiSelector().text(\"OK\")")).isEmpty());
                driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"OK\")")).click();
                // Vänta tills dialogen försvunnit
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(d -> d.findElements(AppiumBy.androidUIAutomator("new UiSelector().text(\"OK\")")).isEmpty());
            }
        } catch (Exception ignored) {}
    }

    @And("användaren rensar formuläret")
    public void rensarFormularet() {
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            waitForElementToBeVisible(taskInputPage.getClearButton()).click();
        } else {
            // Scrollar till clearButton om den hamnat under synlig yta (formuläret är högt med preset-chips)
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(" +
                    "new UiSelector().description(\"clearButton\"))")).click();
        }
    }

    @And("användaren väljer den sparade uppgiften {string}")
    public void väljerSparadUppgift(String name) {
        waitForElementToBeVisible(taskInputPage.getChooseTemplateButton()).click();
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    taskInputPage.getTemplateItems().stream()
                            .anyMatch(e -> { try { return name.equals(e.getText()); } catch (Exception ex) { return false; } }));
            taskInputPage.getTemplateItems().stream()
                    .filter(e -> { try { return name.equals(e.getText()); } catch (Exception ex) { return false; } })
                    .findFirst().ifPresent(WebElement::click);
        } else {
            // Android: getText() returnerar "" för content-desc-element — sök på synlig text
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    !d.findElements(AppiumBy.androidUIAutomator(
                            "new UiSelector().text(\"" + name + "\")")).isEmpty());
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"" + name + "\")")).click();
        }
        // Vänta tills dialogen stängts
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                taskInputPage.getTemplateItems().isEmpty());
    }

    @Then("ska formuläret vara ifyllt med uppgiften {string}")
    public void skaFormularetVaraIfyllt(String expectedName) {
        WebElement taskInput = waitForElementToBeVisible(AppiumBy.accessibilityId("taskInput"));
        assertEquals(expectedName, taskInput.getText(), "Formuläret är inte ifyllt med rätt uppgiftsnamn");
    }

    @And("användaren tar bort den sparade uppgiften {string}")
    public void tarBortSparadUppgift(String name) {
        waitForElementToBeVisible(taskInputPage.getChooseTemplateButton(), 15).click();
        WebElement item;
        int rowIndex;
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
                    taskInputPage.getTemplateItems().stream()
                            .anyMatch(e -> { try { return name.equals(e.getText()); } catch (Exception ex) { return false; } }));
            List<WebElement> itemNames = taskInputPage.getTemplateItems();
            rowIndex = -1;
            for (int i = 0; i < itemNames.size(); i++) {
                try { if (name.equals(itemNames.get(i).getText())) { rowIndex = i; break; } } catch (Exception ignored) {}
            }
            assertTrue(rowIndex >= 0, "Hittade inte mallen '" + name + "' i listan");
            item = itemNames.get(rowIndex);
        } else {
            // Android: containern kan aggregera barntext (namn + kategori) — använd textContains
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    !d.findElements(AppiumBy.androidUIAutomator(
                            "new UiSelector().textContains(\"" + name + "\")")).isEmpty());
            item = driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + name + "\")"));
            waitForElementToBeVisible(item);
            rowIndex = 0;
        }
        // Extrahera koordinater i en retry-loop — React-omrendering kan göra item stale
        // mellan findElement och getRect-anropet
        int[] coords = {0, 0, 0}; // [y, startX, endX]
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            coords[0] = item.getRect().y + item.getRect().height / 2;
            coords[1] = item.getRect().x + item.getRect().width - 10;
            coords[2] = item.getRect().x + 10;
        } else {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        WebElement el = d.findElement(AppiumBy.androidUIAutomator(
                                "new UiSelector().textContains(\"" + name + "\")"));
                        coords[0] = el.getRect().y + el.getRect().height / 2;
                        coords[1] = el.getRect().x + el.getRect().width - 10;
                        coords[2] = el.getRect().x + 10;
                        return true;
                    });
        }
        int y = coords[0], startX = coords[1], endX = coords[2];
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, y));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(800), PointerInput.Origin.viewport(), endX, y));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(swipe));
        tapDeleteAndConfirm("templateDeleteYes", rowIndex, y);
    }

    @Then("ska den sparade uppgiften {string} inte längre finnas i listan")
    public void skaUppgiftenInteLängreFinnasIListan(String name) {
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
                taskInputPage.getTemplateItems().stream()
                        .noneMatch(e -> { try { return name.equals(e.getText()); } catch (Exception ex) { return false; } }));
    }

    @Then("ska bara en sparad uppgift med namnet {string} finnas")
    public void skaBaraEnSparadUppgiftMedNamnet(String name) {
        waitForElementToBeVisible(taskInputPage.getChooseTemplateButton()).click();
        int count;
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    !taskInputPage.getTemplateItems().isEmpty());
            count = (int) taskInputPage.getTemplateItems().stream()
                    .filter(e -> { try { return name.equals(e.getText()); } catch (Exception ex) { return false; } })
                    .count();
        } else {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    !d.findElements(AppiumBy.androidUIAutomator(
                            "new UiSelector().text(\"" + name + "\")")).isEmpty());
            count = driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"" + name + "\")")).size();
        }
        assertEquals(1, count,
                "Förväntade exakt 1 sparad uppgift med namnet '" + name + "' men hittade " + count);
        waitForElementToBeVisible(taskInputPage.getTemplateDialogClose(), 15).click();
    }

    @And("användaren tar bort den senaste historikposten")
    public void tarBortSenasteHistorikpost() {
        // Vänta tills den senaste (översta) historikposten är vår uppgift
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
            List<WebElement> items = historyPage.getTitleElements();
            if (items.isEmpty()) return false;
            try { return items.get(0).getText().contains(lastTaskName); } catch (Exception ex) { return false; }
        });

        List<WebElement> items = historyPage.getTitleElements();

        // Den nya posten är alltid överst (index 0) — garanterat av väntan ovan
        int rowIndex = 0;
        WebElement targetItem = items.get(0);
        int y = targetItem.getRect().y + targetItem.getRect().height / 2;
        int startX = targetItem.getRect().x + targetItem.getRect().width - 10;
        int endX   = targetItem.getRect().x + 10;
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, y));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(800), PointerInput.Origin.viewport(), endX, y));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(swipe));
        tapDeleteAndConfirm("historyDeleteYes", rowIndex, y);
    }

    @When("användaren väljer kategorin {string}")
    public void väljerKategorin(String kategori) {
        waitForElementToBeVisible(taskInputPage.getCategoryButton()).click();
        waitForElementToBeVisible(taskInputPage.categoryItemLocator(kategori), 15).click();
    }

    @Then("ska kategorin {string} vara vald")
    public void skaKategorinVaraVald(String kategori) {
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .ignoring(WebDriverException.class)
                .until(d -> {
                    try { return getElementText(taskInputPage.getSelectedCategoryLabel()).contains(kategori); }
                    catch (Exception e) { return false; }
                });
    }

    @Then("ska historiken inte längre visa uppgiften {string}")
    public void skaHistorikenInteLängreVisaUppgiften(String taskName) {
        // Vi raderade den översta posten — vänta tills position 0 inte längre är taskName
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
            List<WebElement> titles = historyPage.getTitleElements();
            if (titles.isEmpty()) return true;
            try { return !titles.get(0).getText().contains(taskName); } catch (Exception ex) { return false; }
        });
    }

    @When("användaren navigerar till Listor")
    public void navigerarTillListor() {
        waitForElementToBeVisible(AppiumBy.accessibilityId("listorTab")).click();
    }

    @When("användaren skapar en ny lista {string}")
    public void skaparNyLista(String name) {
        waitForElementToBeVisible(checklistsPage.getCreateListButton(), 15).click();
        waitForElementToBeVisible(checklistsPage.getChecklistNameInput(), 15).sendKeys(name);
        waitForElementToBeVisible(checklistsPage.getCreateListConfirmButton(), 15).click();
    }

    @Then("ska detaljvyn för {string} visas")
    public void skaDetaljvynVisas(String name) {
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            // iOS: vänta på backButton som bekräftelse att detaljvyn laddats (checklist != null-guard passerat)
            // Namnverifiering hoppas över — dynamiska accessibilityLabel-värden är opålitliga via XCUITest,
            // och efterföljande steg (lägg till punkt, bocka av) verifierar implicit att rätt lista visas.
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .ignoring(WebDriverException.class)
                    .until(d -> !d.findElements(AppiumBy.accessibilityId("checklistBackButton")).isEmpty());
        } else {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .ignoring(WebDriverException.class)
                        .until(d -> name.equals(getElementText(checklistDetailPage.getChecklistDetailTitle())));
            } catch (TimeoutException e) {
                String actual = safeGetText(checklistDetailPage.getChecklistDetailTitle());
                throw new AssertionError("Detaljvyn visade inte '" + name + "', hittade: '" + actual + "'", e);
            }
        }
    }

    @When("användaren lägger till listpunkten {string}")
    public void läggerTillListpunkten(String text) {
        WebElement input = waitForElementToBeVisible(checklistDetailPage.getAddItemInput());
        input.sendKeys(text);
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            // Tryck Return (onSubmitEditing) istället för att klicka +‑knappen —
            // knappen kan vara mitt i KeyboardAvoidingView‑animation och missa klicket
            input.sendKeys(Keys.RETURN);
        } else {
            checklistDetailPage.getAddItemButton().click();
        }
        By locator = "ios".equalsIgnoreCase(PLATFORM)
                ? AppiumBy.iOSNsPredicateString("label CONTAINS '" + text + "'")
                : AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + text + "\")");
        waitForElementToBeVisible(locator, 15);
    }

    @Then("ska listpunkten {string} finnas i listan")
    public void skaListpunktenFinnasIListan(String text) {
        By locator = "ios".equalsIgnoreCase(PLATFORM)
                ? AppiumBy.iOSNsPredicateString("label CONTAINS '" + text + "'")
                : AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + text + "\")");
        waitForElementToBeVisible(locator, 15);
    }

    @When("användaren bockar av listpunkten {string}")
    public void bockarAvListpunkten(String text) {
        By textLocator = "ios".equalsIgnoreCase(PLATFORM)
                ? AppiumBy.iOSNsPredicateString("label CONTAINS '" + text + "'")
                : AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + text + "\")");
        WebElement textEl = waitForElementToBeVisible(textLocator, 15);
        int targetY = textEl.getRect().y + textEl.getRect().height / 2;

        // Klicka checkboxen i samma rad (närmast i Y-led)
        List<WebElement> checkboxes = driver.findElements(AppiumBy.accessibilityId("checklistItemCheckbox"));
        WebElement closest = checkboxes.stream()
                .min(java.util.Comparator.comparingInt(e -> Math.abs(e.getRect().y - targetY)))
                .orElseThrow(() -> new AssertionError("Ingen checkbox hittad för: " + text));
        closest.click();
    }

    @Then("ska historiken visa listan {string}")
    public void skaHistorikenVisaListan(String listName) {
        skaHistorikenVisaMinst(1, listName);
    }

    @When("användaren tar bort checklistan {string}")
    public void tarBortChecklistan(String name) {
        WebElement item;
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            item = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .ignoring(WebDriverException.class)
                    .until(d -> {
                        List<WebElement> els = d.findElements(
                            AppiumBy.iOSNsPredicateString("label CONTAINS '" + name + "'"));
                        return els.isEmpty() ? null : els.get(0);
                    });
        } else {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                    !checklistsPage.getChecklistItemTitles().isEmpty());
            List<WebElement> titles = checklistsPage.getChecklistItemTitles();
            item = titles.stream()
                    .filter(e -> { try { return e.getText().contains(name); } catch (Exception ex) { return false; } })
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Hittade inte checklistan '" + name + "'"));
        }
        int y = item.getRect().y + item.getRect().height / 2;
        int startX = item.getRect().x + item.getRect().width - 10;
        int endX   = item.getRect().x + 10;
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, y));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(800), PointerInput.Origin.viewport(), endX, y));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(swipe));
        tapDeleteAndConfirm("checklistDeleteYes", 0, y);
    }

    @Then("ska checklistan {string} inte längre finnas i Listor")
    public void skaChecklistanInteLängreFinnasIListor(String name) {
        if ("ios".equalsIgnoreCase(PLATFORM)) {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                d.findElements(AppiumBy.iOSNsPredicateString("label CONTAINS '" + name + "'")).isEmpty());
        } else {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                List<WebElement> titles = checklistsPage.getChecklistItemTitles();
                return titles.stream().noneMatch(e -> {
                    try { return e.getText().contains(name); } catch (Exception ex) { return false; }
                });
            });
        }
    }

    private void tapDeleteAndConfirm(String deleteBtnAccessibilityId, int rowIndex, int targetY) {
        // Vänta tills delete-knappen för rätt rad är synlig (svepanimationen klar)
        WebElement deleteBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    List<WebElement> btns = d.findElements(AppiumBy.accessibilityId(deleteBtnAccessibilityId));
                    return btns.stream()
                            .filter(e -> e.isDisplayed())
                            .min(java.util.Comparator.comparingInt(e -> Math.abs(e.getRect().y - targetY)))
                            .orElse(null);
                });
        assertNotNull(deleteBtn, "Hittade ingen synlig " + deleteBtnAccessibilityId + " efter svep");

        // Vänta tills svepanimationen är klar innan koordinaterna låses
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // X från delete-knappens rect (alltid vid högerkanten), Y från den svepte raden
        int btnX = deleteBtn.getRect().x + deleteBtn.getRect().width / 2;
        int btnY = targetY;

        // PointerInput-tap fungerar på både Android och iOS (mobile: clickGesture är Android-only)
        PointerInput tapFinger = new PointerInput(PointerInput.Kind.TOUCH, "tapFinger");
        Sequence tap = new Sequence(tapFinger, 0);
        tap.addAction(tapFinger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), btnX, btnY));
        tap.addAction(tapFinger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(tapFinger.createPointerMove(Duration.ofMillis(50), PointerInput.Origin.viewport(), btnX, btnY));
        tap.addAction(tapFinger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(tap));

        // positiv-knappen i RN Alert — plattformsberoende
        // Android: Material Design uppgraderar knapptext till versaler ("JA")
        // iOS: behåller originaltext ("Ja")
        By jaBy = "ios".equalsIgnoreCase(PLATFORM)
                ? By.xpath("//XCUIElementTypeButton[@name='Ja']")
                : AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"[Jj][Aa]\")");
        waitForElementToBeVisible(jaBy, 10).click();
    }

    private WebElement waitForElementToBeVisible(WebElement element) {
        return waitForElementToBeVisible(element, 5);
    }

    private WebElement waitForElementToBeVisible(WebElement element, int timeoutSeconds) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            throw new AssertionError("waitForElementToBeVisible: elementet syntes inte inom " + timeoutSeconds + "s [" + element + "]", e);
        }
    }

    private WebElement waitForElementToBeVisible(By locator) {
        return waitForElementToBeVisible(locator, 5);
    }

    private WebElement waitForElementToBeVisible(By locator, int timeoutSeconds) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new AssertionError("waitForElementToBeVisible: hittade inte element [" + locator + "] inom " + timeoutSeconds + "s", e);
        }
    }

    private String getElementText(WebElement element) {
        return element.getText();
    }

    private void waitForText(WebElement element, String expectedText) {
        waitForTextIgnoringWDE(element, expectedText);
    }

    private void waitForTextIgnoringWDE(WebElement element, String expectedText) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .ignoring(WebDriverException.class)
                    .until(d -> {
                        try {
                            return expectedText.equals(getElementText(element));
                        } catch (Exception e) {
                            return false;
                        }
                    });
        } catch (TimeoutException e) {
            String actual;
            try { actual = getElementText(element); } catch (Exception ex) { actual = "<kunde inte läsa: " + ex.getMessage() + ">"; }
            throw new AssertionError(
                    "waitForText: förväntade '" + expectedText + "' men hittade '" + actual + "'", e);
        }
    }

    private String safeGetText(WebElement element) {
        try { return getElementText(element); } catch (Exception e) { return "<kunde inte läsa: " + e.getMessage() + ">"; }
    }

    @After
    public void tearDown(Scenario scenario) {
        if (driver != null && scenario.isFailed()) {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Screenshot on failure");
        }
        // Ge swiftshader-renderaren tid att städa GPU-buffrar mellan scenarion
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
