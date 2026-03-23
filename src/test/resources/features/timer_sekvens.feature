Feature: Timer-app underuppgiftssekvens

  @sekvens
  Scenario Outline: Underuppgifter genomförs i rätt ordning, fortsätt och avsluta
    Given appen är startad
    When användaren fyller i uppgiften "<uppgift>"
    And användaren lägger till underuppgift "<underuppgift1>"
    And användaren lägger till underuppgift "<underuppgift2>"
    And användaren sätter tid till "<tid>"
    And användaren sätter paus-tid till "<pausTid>"
    And användaren startar sekvensen
    Then ska timern visa "JOBBA!" för underuppgift 1 av 2
    When timern räknar ner klart
    Then ska timern visa paus med nästa underuppgift 2
    When timern räknar ner klart
    Then ska timern visa "JOBBA!" för underuppgift 2 av 2
    When timern räknar ner klart
    Then ska fortsätt-skärmen visa rätt tid
    When användaren klickar på "continueYesButton"
    Then ska timern visa "JOBBA!" för underuppgift 1 av 2
    When timern räknar ner klart
    Then ska timern visa paus med nästa underuppgift 2
    When timern räknar ner klart
    Then ska timern visa "JOBBA!" för underuppgift 2 av 2
    When timern räknar ner klart
    Then ska fortsätt-skärmen visa rätt tid
    When användaren klickar på "continueNoButton"
    Then ska historiken visa minst 2 körningar med uppgiften "<uppgift>"

    Examples:
      | uppgift  | underuppgift1 | underuppgift2       | tid | pausTid |
      | Läsa bok | Öppna boken   | Läs 2 sidor i boken | 30  | 30      |

  @mall
  Scenario Outline: Spara, välj och ta bort en sparad uppgift
    Given appen är startad
    When användaren fyller i uppgiften "<uppgift>"
    And användaren lägger till underuppgift "<underuppgift>"
    And användaren sätter tid till "<tid>"
    And användaren sätter paus-tid till "<tid>"
    And användaren sparar uppgiften
    And användaren rensar formuläret
    Then ska uppgiftsnamnet vara tomt
    And ska underuppgifterna vara borta
    And ska tiderna vara återställda till "<standardtid>"
    When användaren väljer den sparade uppgiften "<uppgift>"
    Then ska formuläret vara ifyllt med uppgiften "<uppgift>"
    And ska underuppgiften "<underuppgift>" finnas i formuläret
    And ska tiderna vara satta till "<tid>"
    When användaren startar sekvensen
    Then ska timern visa "JOBBA!" för underuppgift 1 av 1
    When timern räknar ner klart
    Then ska fortsätt-skärmen visa rätt tid
    When användaren klickar på "continueNoButton"
    And användaren klickar på "newTaskButton"
    And användaren tar bort den sparade uppgiften "<uppgift>"
    Then ska den sparade uppgiften "<uppgift>" inte längre finnas i listan
    And användaren stänger dialogen

    Examples:
      | uppgift | underuppgift | tid | standardtid |
      | Testar  | Lite testing | 8   | 120         |

  @kantfall
  Scenario Outline: Uppgiftsnamn med <antal> tecken trunkeras till 50
    Given appen är startad
    When användaren fyller i ett uppgiftsnamn på <antal> tecken
    Then ska feltext för uppgiftsnamnet visas
    When användaren lägger till underuppgiften på <antal> tecken
    Then ska feltext för underuppgiftsnamnet visas
    When användaren bekräftar underuppgiften
    And användaren sätter tid till "<tid>"
    And användaren sätter paus-tid till "<tid>"
    And användaren startar sekvensen
    Then ska timern visa "JOBBA!" för underuppgift 1 av 1
    When ska underuppgiftsnamnet i timern ha längden 50 tecken
    And timern räknar ner klart
    Then ska uppgiftsnamnet på fortsätt-skärmen ha längden 50 tecken
    When användaren klickar på "continueNoButton"
    Then ska historiken visa 1 körning med det angivna uppgiftsnamnet
    And ska uppgiftsnamnet i historiken ha längden 50 tecken

    Examples:
      | antal | tid |
      | 60    | 8   |

  @animation
  Scenario Outline: Animationerna visas på rätt skärmar
    Given appen är startad
    When användaren fyller i uppgiften "<uppgift>"
    And användaren lägger till underuppgift "<underuppgift1>"
    And användaren lägger till underuppgift "<underuppgift2>"
    And användaren sätter tid till "<tid>"
    And användaren sätter paus-tid till "<tid>"
    And användaren startar sekvensen
    Then ska jobba-animationen visas och paus-animationen vara dold
    When timern räknar ner klart
    Then ska paus-animationen visas och jobba-animationen vara dold
    When timern räknar ner klart
    Then ska jobba-animationen visas och paus-animationen vara dold
    When timern räknar ner klart
    When användaren klickar på "continueNoButton"

    Examples:
      | uppgift  | underuppgift1 | underuppgift2 | tid |
      | AnimTest | Uppgift ett   | Uppgift två   | 20  |

  @duplikat
  Scenario Outline: Det går inte att spara en uppgift med ett redan använt namn
    Given appen är startad
    When användaren fyller i uppgiften "<uppgift>"
    And användaren lägger till underuppgift "<underuppgift>"
    And användaren sparar uppgiften
    And användaren sparar uppgiften
    Then ska bara en sparad uppgift med namnet "<uppgift>" finnas
    And användaren tar bort den sparade uppgiften "<uppgift>"
    And användaren stänger dialogen

    Examples:
      | uppgift      | underuppgift    |
      | DuplikatTest | En underuppgift |

  @historik
  Scenario Outline: En historisk post kan tas bort
    Given appen är startad
    When användaren fyller i uppgiften "<uppgift>"
    And användaren lägger till underuppgift "<underuppgift>"
    And användaren sätter tid till "<tid>"
    And användaren sätter paus-tid till "<tid>"
    And användaren startar sekvensen
    Then ska timern visa "JOBBA!" för underuppgift 1 av 1
    When timern räknar ner klart
    Then ska fortsätt-skärmen visa rätt tid
    When användaren klickar på "continueNoButton"
    And användaren tar bort den senaste historikposten
    Then ska historiken inte längre visa uppgiften "<uppgift>"

    Examples:
      | uppgift  | underuppgift      | tid |
      | BortTest | Testa borttagning |  8  |
