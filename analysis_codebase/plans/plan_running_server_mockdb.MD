# Plan: Server starten met Mock Database en Statische Testdata

## Doel
De applicatie moet kunnen opstarten **zonder een actieve MariaDB-database**. In dat geval valt de applicatie terug op een **mock database** die **statische testdata** uit een bestand laadt. In de console moet een **duidelijke waarschuwing in HOOFDLETTERS** worden getoond dat de applicatie op testdata werkt.

Dit maakt het mogelijk om handmatig het systeem te evalueren, te demonstreren of te ontwikkelen zonder afhankelijk te zijn van een echte database.

---

## Uitgangspunt
De huidige architectuur gebruikt al:
- **Spring Data JPA** met repository-interfaces.
- **Dependency Injection** van repositories in services.
- **Spring-profielen** voor omgevingsafhankelijke configuratie.

Dit biedt een goede basis om mock-implementaties van repositories te introduceren onder een apart profiel.

---

## Gevolgde principes
- **Geen impact op productiecode** wanneer het `mock-db`-profiel niet actief is.
- **Testdata wordt centraal beheerd** in een makkelijk aanpasbaar bestand.
- **Duidelijke waarschuwing** bij opstarten in mock-modus.
- **Behoud van de bestaande service- en controllerlagen**: alleen de data-laag wordt vervangen.

---

## Stap-voor-stap plan

### Stap 1: Kies formaat voor testdata-bestand
**Aanbeveling: YAML of JSON**

Voordelen YAML:
- Beter leesbaar voor handmatig beheer.
- Ondersteunt commentaar.

Voordelen JSON:
- Geen extra dependency nodig, Jackson ondersteunt JSON out-of-the-box.
- YAML vereist de `jackson-dataformat-yaml` dependency.

**Beslissing:** Gebruik **JSON** om afhankelijkheden te minimaliseren, tenzij YAML de voorkeur heeft vanwege leesbaarheid.

**Bestandslocatie:**
```
ClimateChecker/src/main/resources/testdata/testdata.json
```

---

### Stap 2: Maak structuur voor testdata
Het testdata-bestand bevat entiteiten die overeenkomen met de bestaande JPA-entiteiten.

Voorbeeld voor `testdata.json`:
```json
{
  "users": [
    {
      "userId": 1,
      "email": "admin@example.com",
      "password": "$2a$10$...",
      "firstName": "Admin",
      "lastName": "User",
      "role": "SUPER_ADMIN"
    }
  ],
  "stations": [
    {
      "stationId": 1,
      "name": "Test Station",
      "databaseTag": "TS1",
      "isPublic": true,
      "registrationCode": 123456
    }
  ],
  "neighbourhoods": [
    {
      "neighbourhoodId": 1,
      "name": "Testbuurt",
      "regionId": 1
    }
  ],
  "tokens": [],
  "workshops": []
}
```

**Actiepunten:**
- Inventariseer alle entiteiten met repositories.
- Bepaal voor elke entiteit welke velden nodig zijn voor handmatige evaluatie.
- Vul het JSON-bestand met realistische testdata.

---

### Stap 3: Implementeer `TestDataLoader`
Maak een component die het JSON-bestand inleest en de data beschikbaar maakt.

**Locatie:**
```
ClimateChecker/src/main/java/Ontdekstation013/ClimateChecker/mockdb/TestDataLoader.java
```

**Verantwoordelijkheden:**
- Inlezen van `testdata.json` bij opstarten.
- Converteren van JSON naar een generieke structuur (`Map<String, List<Map<String, Object>>>`).
- Aanbieden van methoden om data per entiteit op te vragen.

**Aandachtspunten:**
- Component moet actief zijn onder het `mock-db`-profiel.
- Foutafhandeling bij ontbrekend of ongeldig JSON-bestand.


---

### Stap 4: Implementeer mock repositories
Maak voor elke bestaande repository een mock-implementatie die het `mock-db`-profiel gebruikt.

**Locatie per feature:**
```
ClimateChecker/src/main/java/Ontdekstation013/ClimateChecker/features/user/MockUserRepository.java
ClimateChecker/src/main/java/Ontdekstation013/ClimateChecker/features/station/MockStationRepository.java
ClimateChecker/src/main/java/Ontdekstation013/ClimateChecker/features/neighbourhood/MockNeighbourhoodRepository.java
ClimateChecker/src/main/java/Ontdekstation013/ClimateChecker/features/workshop/MockWorkshopRepository.java
ClimateChecker/src/main/java/Ontdekstation013/ClimateChecker/features/user/authentication/MockTokenRepository.java
```

**Eigenschappen van elke mock repository:**
- Annotatie: `@Repository` en `@Profile("mock-db")`.
- Implementeert de bestaande repository-interface.
- Laadt testdata via `TestDataLoader`.
- Converteert generieke maps naar de juiste entiteitsklassen.
- Ondersteunt standaard CRUD-operaties op basis van de data in het geheugen.

**Belangrijk:** De mock repository moet voldoende methoden implementeren zodat services zonder wijzigingen blijven werken.

---

### Stap 5: Voeg console-waarschuwing toe
Maak een `CommandLineRunner` die alleen actief is onder het `mock-db`-profiel en een waarschuwing in HOOFDLETTERS toont.

**Locatie:**
```
ClimateChecker/src/main/java/Ontdekstation013/ClimateChecker/mockdb/MockDatabaseWarningRunner.java
```

**Voorbeeld output:**
```
================================================================================
||                                                                            ||
||  ⚠️  WAARSCHUWING: DEZE APPLICATIE DRAAIT OP TESTDATA UIT EEN BESTAND.     ||
||  ⚠️  DE DATABASE IS NIET ACTIEF.                                           ||
||  ⚠️  ALLE WIJZIGINGEN ZIJN TIJDELIJK EN WORDEN NIET OPGESLAGEN.            ||
||                                                                            ||
================================================================================
```

**Aandachtspunten:**
- Gebruik `System.out.println` of een logger met een duidelijk onderscheidend niveau.
- Zorg dat de waarschuwing bovenaan de console verschijnt, bij voorkeur als onderdeel van een `CommandLineRunner`.

---

### Stap 6: Configureer `mock-db` profiel
Maak een aparte Spring-configuratie voor het mock-profiel.

**Locatie:**
```
ClimateChecker/src/main/resources/application-mock-db.properties
```

**Inhoud:**
```properties
# Schakel automatische database-configuratie uit
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=

# Zorg dat JPA geen verbinding probeert te maken
spring.jpa.hibernate.ddl-auto=none
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Sta bean-overschrijving toe zodat mock repositories de Spring Data repositories vervangen
spring.main.allow-bean-definition-overriding=true
```

**Aandachtspunten:**
- De applicatie moet zonder fouten opstarten zonder geldige datasource.
- Mock repositories moeten de Spring Data-implementaties overschrijven.


---

### Stap 7: Zorg voor automatische fallback
**Optioneel maar aanbevolen:** detecteer of de database beschikbaar is en schakel automatisch naar het `mock-db`-profiel.

**Mogelijke aanpak:**
- Een `EnvironmentPostProcessor` of `ApplicationContextInitializer` controleert de database-verbinding.
- Als geen verbinding mogelijk is, wordt `spring.profiles.active` dynamisch uitgebreid met `mock-db`.
- Alternatief: documenteer expliciet dat ontwikkelaars het profiel handmatig moeten activeren.

**Voordeel handmatige activatie:**
- Eenvoudiger en voorspelbaarder.
- Geen risico dat productie onbedoeld in mock-modus valt.

**Aanbeveling:** Begin met **handmatige activatie** van het profiel. Automatische fallback kan later worden toegevoegd.

---

### Stap 8: Update Maven/Gradle-configuratie
Controleer of er geen onnodige runtime-afhankelijkheden zijn die de applicatie verplicht een database vereisen.

**Actiepunten:**
- Controleer `pom.xml` op de aanwezigheid van `spring-boot-starter-data-jpa` en de MariaDB-driver.
- Deze dependencies blijven nodig, maar de applicatie moet ook zonder actieve verbinding kunnen opstarten.
- Voeg indien nodig `jackson-databind` toe (meestal al aanwezig via Spring Boot).

---

### Stap 9: Documentatie
Voeg instructies toe zodat ontwikkelaars en testers weten hoe ze de mock-modus gebruiken.

**Locatie:**
- `ClimateChecker/README.md` of een apart bestand `ClimateChecker/docs/MOCK_DATABASE.md`.

**Inhoud:**
- Hoe start je de applicatie met testdata?
- Waar bevindt het testdata-bestand zich?
- Hoe pas je testdata aan?
- Wat zijn de beperkingen (bijv. wijzigingen worden niet opgeslagen)?
- Voorbeeld opstartcommando:
  ```bash
  mvn spring-boot:run -Dspring-boot.run.profiles=mock-db
  ```

---

### Stap 10: Validatie
Controleer na implementatie de volgende punten:

- [ ] Applicatie start zonder fouten op met `--spring.profiles.active=mock-db`.
- [ ] Console toont de waarschuwing in HOOFDLETTERS.
- [ ] API-endpoints retourneren de testdata uit `testdata.json`.
- [ ] Aanpassingen in `testdata.json` zijn na herstart zichtbaar.
- [ ] Zonder `mock-db`-profiel start de applicatie normaal op met MariaDB.
- [ ] Productiegedrag is ongewijzigd wanneer het profiel niet actief is.


---

## Bestanden die aangepast of toegevoegd moeten worden

### Nieuwe bestanden
| Bestand | Doel |
|---------|------|
| `src/main/resources/testdata/testdata.json` | Centraal testdata-bestand |
| `src/main/java/.../mockdb/TestDataLoader.java` | Inlezen testdata |
| `src/main/java/.../mockdb/MockDatabaseWarningRunner.java` | Console-waarschuwing |
| `src/main/java/.../features/user/MockUserRepository.java` | Mock implementatie |
| `src/main/java/.../features/station/MockStationRepository.java` | Mock implementatie |
| `src/main/java/.../features/neighbourhood/MockNeighbourhoodRepository.java` | Mock implementatie |
| `src/main/java/.../features/workshop/MockWorkshopRepository.java` | Mock implementatie |
| `src/main/java/.../features/user/authentication/MockTokenRepository.java` | Mock implementatie |
| `src/main/resources/application-mock-db.properties` | Configuratie mock-profiel |
| `docs/MOCK_DATABASE.md` of `README.md` update | Documentatie |

### Bestaande bestanden die mogelijk aangepast moeten worden
| Bestand | Aanpassing |
|---------|------------|
| `pom.xml` | Controleer Jackson-afhankelijkheden |
| `README.md` | Voeg gebruiksinstructies toe |

---

## Risico's en mitigaties

| Risico | Mitigatie |
|--------|-----------|
| Productie draait per ongeluk op testdata | Mock-profiel is handmatig te activeren; gebruik geen automatische fallback in productie |
| Testdata is onvolledig | Inventariseer alle repositories en zorg voor representatieve data |
| Services gebruiken niet-gemockte methoden | Implementeer alle methoden uit de repository-interfaces |
| Console-waarschuwing wordt over het hoofd gezien | Gebruik HOOFDLETTERS, visuele scheiding en plaats bovenaan de logs |

---

## Conclusie
Dit plan beschrijft hoe de ClimateChecker-applicatie kan worden uitgebreid met een **mock database-modus** waarbij statische testdata uit een bestand wordt geladen. De oplossing maakt gebruik van het bestaande Spring Data JPA-repository-patroon en voegt een duidelijke console-waarschuwing toe. De uitwerking heeft geen invloed op de normale werking met MariaDB wanneer het mock-profiel niet actief is.

