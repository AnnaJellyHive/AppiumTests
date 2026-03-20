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

## Elementlokalisering

- Använd i första hand `accessibilityId` eller `id`
- Använd `xpath` bara när `accessibilityId`/`id` inte fungerar
- Deklarera element i page-klasserna med `@AndroidFindBy` (och `@iOSXCUITFindBy` för iOS)
- Page objects använder `AppiumFieldDecorator(Duration.ZERO)` — viktigt för att `WebDriverWait` ska kunna polla snabbt

## CI-pipeline

`.github/workflows/appium-tests.yml` checkar ut TimerApp, bundlar JS med `npx react-native bundle` (inget Metro i CI), bygger APK och kör på Android API 33-emulator med swiftshader.

Timeouts i `setUp()` är avsiktligt höga på grund av långsam CI-emulator (swiftshader, ~8 min att boota):
- `uiautomator2ServerInstallTimeout` / `uiautomator2ServerLaunchTimeout` / `adbExecTimeout` = 120s
- `startButton`-väntan = 120s

Sänk inte dessa utan att ha verifierat att CI-körningar klarar det.

## React Native-specifikt

- Text-element hittas INTE med `AppiumBy.accessibilityId()` (använder `UiSelector().description()`) — använd `@AndroidFindBy(xpath = "//*[@content-desc='...']")` för Text-komponenter
- `content-desc` på `ViewGroup` innehåller ofta ingen text — texten sitter på ett syskon-`TextView`, inte på barnet
