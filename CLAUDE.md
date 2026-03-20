# AppiumTests – projektinstruktioner

## Elementlokalisering
- Använd i första hand `id` eller `accessibilityId` för att hitta element
- Använd `xpath` bara som sista utväg när id/accessibilityId inte fungerar

## Page objects
- Deklarera element med `@AndroidFindBy` (och `@iOSXCUITFindBy` för iOS) i page-klasserna
- Undvik att anropa `driver.findElement(...)` direkt i steg-klasser när ett page object kan användas
