# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Köra tester

```bash
# Kör alla tester (kräver Appium-server + emulator på emulator-5554)
mvn test

# Kör ett specifikt scenario med tag
mvn test -Dcucumber.filter.tags="@sekvens"
```

Appium-servern måste vara igång (`appium`) och appen installerad på `emulator-5554` innan testerna körs.

## Arkitektur

Cucumber BDD med JUnit Platform. Tre lager:

- **Feature-filer** (`src/test/resources/features/`) — scenarier på svenska med Gherkin
- **Step-definitioner** (`TimerSteps.java`) — ett enda steg-klass med `@Before`/`@After` som hanterar driver-livscykel
- **Page objects** (`pages/`) — `TaskInputPage`, `TimerPage`, `ContinuePage`, `HistoryPage`

`TimerSteps.setUp()` sätter två kritiska Appium-inställningar:
- `waitForIdleTimeout = 0` — React Native uppdaterar UI kontinuerligt (timer), utan detta blockerar klick tills UI:t är stilla
- `implicitlyWait(ZERO)` — globalt, så att `WebDriverWait`-loopar kan polla snabbt utan att varje `findElement` väntar 10s

## Vänta på element

Använd alltid hjälpmetoderna i `TimerSteps` — aldrig `driver.findElement()` direkt i steg:

```java
waitForElementToBeVisible(WebElement element)   // väntar tills synligt
waitForElementToBeVisible(By locator)           // samma, med locator
waitForText(WebElement element, String text)    // väntar tills getText() matchar
```

Alla hjälpmetoder kastar `AssertionError` (inte `TimeoutException`) vid timeout, med faktiskt hittad text i meddelandet — t.ex. `waitForText: förväntade 'JOBBA!' men hittade 'VILA!'`. Detta syns tydligt i HTML-rapporten.

`skaTimernVisaJobba` kontrollerar mode, task och progress **atomärt i ett enda poll** — inte tre sekventiella väntan. Annars riskerar appen att övergå till nästa fas mellan kontrollerna på CI.

## Elementlokalisering

- Använd i första hand `accessibilityId` eller `id`
- Använd `xpath` bara när `accessibilityId`/`id` inte fungerar
- Deklarera element i page-klasserna med `@AndroidFindBy` (och `@iOSXCUITFindBy` för iOS)
- Page objects använder `AppiumFieldDecorator(Duration.ZERO)` — viktigt för att `WebDriverWait` ska kunna polla snabbt

## CI-pipeline

`.github/workflows/appium-tests.yml` checkar ut TimerApp, bundlar JS med `npx react-native bundle` (inget Metro i CI), bygger APK och kör på Android API 33-emulator med swiftshader.

Timeouts i `setUp()` är avsiktligt höga på grund av långsam CI-emulator (swiftshader, ~8 min att boota):
- `uiautomator2ServerInstallTimeout` / `uiautomator2ServerLaunchTimeout` / `adbExecTimeout` = 120s
- `startButton`-väntan = 240s

Sänk inte dessa utan att ha verifierat att CI-körningar klarar det.

`startButton`-waiten ignorerar `WebDriverException` — UiAutomator2 kastar det när React Native håller UI-tråden upptagen under JS-initialisering. Utan detta kraschar waiten direkt istället för att försöka igen.

`startButton`-waiten stänger även "System UI isn't responding"-dialoger automatiskt genom att klicka "Wait" i varje poll-iteration. `hide_error_dialogs=1` undertrycker inte just den dialogen på CI.

APK:n cachas baserat på TimerApp-källkod + gradle-filer — om bara testkod ändras hoppas hela bygget över. Appium-drivern (`~/.appium`) cachas baserat på workflow-filens hash.

Kör CI manuellt (utan att pusha) via GitHub UI: Actions → Appium Tests → Run workflow, eller:
```bash
gh workflow run appium-tests.yml --repo AnnaJellyHive/AppiumTests
```

## React Native-specifikt

- Text-element hittas INTE med `AppiumBy.accessibilityId()` (använder `UiSelector().description()`) — använd `@AndroidFindBy(xpath = "//*[@content-desc='...']")` för Text-komponenter
- `content-desc` på `ViewGroup` innehåller ofta ingen text — texten sitter på ett syskon-`TextView`, inte på barnet
- `TimerScreen` startar timern via `useFocusEffect` (inte på mount) — på CI tar navigationsanimationen flera sekunder, och timern tickade ner under den tiden innan skärmen var synlig för Appium
