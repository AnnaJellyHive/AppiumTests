# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

- Deklarera element i page-klasserna med `@AndroidFindBy` (och `@iOSXCUITFindBy` för iOS) — använd aldrig `driver.findElement()` direkt i steg-klassen när ett page object-fält kan användas istället
- Page objects använder `AppiumFieldDecorator(Duration.ZERO)` — viktigt för att `WebDriverWait` ska kunna polla snabbt

### Android: UiSelector för textinnehåll

React Native Text-element exponerar textinnehållet på olika sätt beroende på API-nivå:
- **API 33 (CI-emulator):** textinnehållet ligger i `@content-desc`
- **API 36 (lokal emulator):** textinnehållet ligger i `@text`

Xpath-attributen `@text` och `@content-desc` är API-beroende — använd aldrig dessa för att matcha textinnehåll. Använd `UiSelector` som är konsekvent på alla API-nivåer:

```java
// Exakt match
AppiumBy.androidUIAutomator("new UiSelector().text(\"Max 50 tecken\")")

// Delsträngsmatch (t.ex. chip med prefix)
AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + subtask + "\")")
```

### Android: Alert-knappar

React Native `Alert.alert` renderar knappar vars text uppgraderas till versaler av Material Design-temat (`"Ja"` → `"JA"`). `By.id("android:id/button1")` fungerar inte — RN Alert är inte en native AlertDialog.

```java
// Android — versaler p.g.a. Material Design
AppiumBy.androidUIAutomator("new UiSelector().text(\"JA\")")
// iOS — originaltext
By.xpath("//XCUIElementTypeButton[@name='Ja']")
```

Om CI-emulator (API 33) visar "Ja" istället för "JA": använd `textMatches("[Jj][Aa]")` (character classes — inte `(?i)` inline-flagga, fungerar inte i alla UIAutomator2-versioner).

### iOS: accessibilityLabel vs testID

`accessibility id`-strategin på iOS (`@iOSXCUITFindBy(accessibility = "xxx")`) matchar mot **accessibilityLabel**, inte mot `testID`.

När ett element har `testID` men ingen `accessibilityLabel` på iOS — använd `iOSNsPredicate`:

```java
@iOSXCUITFindBy(iOSNsPredicate = "identifier == 'timerModeLabel'")
```

`identifier` i NSPredicate matchar iOS `accessibilityIdentifier` = React Natives `testID`.

I TimerPage gäller detta `timerModeLabel`, `timerTaskName`, `timerProgress` — de har `testID` men `accessibilityLabel` satt bara på Android:
```jsx
accessibilityLabel={Platform.OS === 'android' ? 'timerModeLabel' : undefined}
```

Om ett element inte hittas på iOS: fixa lokalisatorn i page-filen — inte i `TimerSteps.java`.

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

- `content-desc` på `ViewGroup` innehåller ofta ingen text — texten sitter på ett syskon-`TextView`, inte på barnet
- `TimerScreen` startar timern via `useFocusEffect` (inte på mount) — på CI tar navigationsanimationen flera sekunder, och timern tickade ner under den tiden innan skärmen var synlig för Appium
