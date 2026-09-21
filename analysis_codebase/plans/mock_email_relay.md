## **Definitief Plan met Geïntegreerde Mitigaties (Overzicht)**

---

## **1. Probleemanalyse**
### **Huidige situatie**
- De backend gebruikt **Spring Boot** en **JavaMailSender** voor het versturen van e-mails via een externe mailserver.
- De mailserver-configuratie wordt gedaan via omgevingsvariabelen (`MAILSERVER_RELAY_HOST`, `MAILSERVER_RELAY_PORT`, etc.).
- `StationEndToEndTests` faalt omdat deze variabelen niet zijn ingesteld, waardoor `JavaMailSender` niet kan initialiseren.
- Er is **geen mechanisme** om te schakelen tussen een echte mailserver en een mock/in-memory mailserver.

### **Probleem**
- Tests vereisen **altijd** een echte mailserver, wat onpraktisch is in lokale ontwikkelomgevingen en CI/CD-pipelines.
- Er is geen **flag** om te schakelen tussen een echte mailserver en een testoplossing.

---

## **2. Oplossingsrichting**
### **Hybride aanpak**
- **Unit tests**: Blijven gebruikmaken van **Mockito** (lichtgewicht, geen externe afhankelijkheden).
- **End-to-end tests**: Gebruiken **GreenMail** (in-memory mailserver voor realistischere tests).
- **Flag**: Voeg een omgevingsvariabele (`USE_REAL_MAILSERVER`) toe om te schakelen tussen een echte mailserver en GreenMail/Mockito.

### **Waarom GreenMail?**
- Specifiek ontworpen voor **testing** en vereist geen externe afhankelijkheden.
- Realistischer dan mocking, omdat e-mails daadwerkelijk worden verzonden (maar niet naar een externe server).
- Lichtgewicht en eenvoudig te integreren in Spring Boot-tests.

### **Waarom Mockito?**
- Wordt al gebruikt in `StationMonitorServiceTest` en is geschikt voor **unit tests**.
- Geen externe afhankelijkheden nodig.

---

## **3. Stappenplan met Mitigaties**
### **Stap 1: Voeg GreenMail toe aan `pom.xml`**
- Voeg GreenMail toe als **test-dependency** om ervoor te zorgen dat deze alleen in tests wordt gebruikt.
- **Mitigaties**:
  - Gebruik een **stabiele versie** van GreenMail die compatibel is met Spring Boot 3.3.5.
  - Voeg een **opmerking** toe in de `pom.xml` om aan te geven waarom GreenMail wordt gebruikt en wanneer deze moet worden bijgewerkt.
  - Zorg ervoor dat GreenMail **alleen** in de `test`-scope wordt gebruikt, zodat deze niet in productie terechtkomt.

---

### **Stap 2: Configureer `JavaMailSender` voor tests**
- Maak een **test-specifieke configuratie** in `src/test/resources/application.properties` om GreenMail te gebruiken.
- **Mitigaties**:
  - Voeg **validatie** toe in de tests om te controleren of GreenMail correct is gestart.
  - Gebruik een **vaste poort** (3025) om conflicten te voorkomen.
  - Zorg ervoor dat GreenMail na elke test wordt gestopt om geheugenlekken te voorkomen.
  - Gebruik **standaard inloggegevens** die niet overeenkomen met productiegegevens.

---

### **Stap 3: Implementeer GreenMail in `StationEndToEndTests`**
- Start GreenMail **voor** de tests en stop het **na** de tests.
- Configureer GreenMail om e-mails te accepteren en te valideren.
- **Mitigaties**:
  - Voeg **validatie** toe in `@BeforeEach` om te controleren of GreenMail correct is gestart.
  - Reset GreenMail na elke test (`@AfterEach`) om een schone staat te garanderen.
  - Zorg ervoor dat GreenMail **niet** wordt blootgesteld aan externe netwerken.

---

### **Stap 4: Voeg een flag toe om te schakelen tussen een echte mailserver en GreenMail**
- Voeg een omgevingsvariabele (`USE_REAL_MAILSERVER`) toe om te schakelen tussen een echte mailserver en GreenMail.
- Pas de configuratie van `JavaMailSender` aan om deze flag te respecteren.
- **Mitigaties**:
  - Gebruik **default waarden** die veilig zijn voor tests (bijvoorbeeld `localhost` en poort `3025`).
  - Zorg ervoor dat de **default waarden** niet overeenkomen met productiegegevens.
  - Documenteer de flag en de default waarden in de `README.md`.

---

### **Stap 5: Pas `EmailSenderService` aan om de flag te respecteren**
- Pas `EmailSenderService` aan om **geen e-mails te versturen** als `USE_REAL_MAILSERVER=false`.
- **Mitigaties**:
  - Voeg **logging** toe om duidelijk te maken wanneer e-mails worden overgeslagen.
  - Zorg ervoor dat de **flag** standaard `false` is in tests, zodat er geen e-mails naar een echte mailserver worden verzonden.
  - Test de wijziging grondig om ervoor te zorgen dat de bestaande functionaliteit niet wordt gebroken.

---

### **Stap 6: Test de oplossing**
- Draai `mvn test` **zonder** `MAILSERVER_RELAY_HOST` om te controleren of de tests slagen.
- Draai `mvn test` **met** `MAILSERVER_RELAY_HOST` om te controleren of de echte mailserver nog steeds werkt.
- **Mitigaties**:
  - Voeg een **test** toe die controleert of de flag correct werkt in zowel test- als productieomgevingen.
  - Monitor de testuitvoeringstijd om ervoor te zorgen dat GreenMail geen significante vertraging veroorzaakt.

---

### **Stap 7: Documentatie**
- Documenteer hoe de flag werkt en hoe ontwikkelaars/tests kunnen schakelen tussen een echte mailserver en GreenMail.
- Voeg een **sectie toe aan de `README.md`** met:
  - Uitleg over de flag `USE_REAL_MAILSERVER`.
  - Stappen om GreenMail lokaal te gebruiken.
  - Stappen om een echte mailserver te gebruiken.
- **Mitigaties**:
  - Zorg ervoor dat de documentatie **up-to-date** blijft bij wijzigingen in de codebase.
  - Voeg **voorbeelden** toe van hoe de flag kan worden ingesteld in verschillende omgevingen (lokaal, CI, productie).

---

## **4. Risico- en Impactanalyse**
### **Risico's en Mitigaties**
| Risico | Beschrijving | Mitigatie |
|--------|--------------|-----------|
| **Compatibiliteit** | GreenMail of de nieuwe flag kan incompatibel zijn met bestaande code. | Gebruik een stabiele versie van GreenMail, test grondig, en documenteer compatibiliteitsvereisten. |
| **Configuratie-fouten** | Verkeerde configuratie van GreenMail of de flag kan leiden tot falende tests. | Voeg validatie toe in tests, gebruik default waarden, en documenteer de configuratie. |
| **Performance** | GreenMail kan de testuitvoering vertragen. | Reset GreenMail na elke test, gebruik een vaste poort, en monitor de testuitvoeringstijd. |
| **Onderhoud** | GreenMail en de nieuwe flag vereisen onderhoud bij updates van de codebase. | Voeg logging toe, documenteer de wijzigingen, en houd de documentatie up-to-date. |
| **Veiligheid** | GreenMail draait een lokale SMTP-server, wat een potentieel beveiligingsrisico kan zijn. | Gebruik alleen de `test`-scope voor GreenMail, gebruik standaard inloggegevens, en zorg ervoor dat de flag standaard `false` is in tests. |

---

### **Impact**
| Impactgebied | Beschrijving |
|--------------|--------------|
| **Testomgeving** | Tests kunnen nu **zonder een echte mailserver** draaien, wat de betrouwbaarheid en snelheid van de CI-pipeline verbetert. |
| **Ontwikkelaars** | Ontwikkelaars kunnen lokaal werken zonder een mailserver te hoeven configureren. |
| **Productie** | Geen impact op productie, omdat de flag standaard `false` is in tests en alleen `true` is als expliciet ingesteld. |
| **Onderhoud** | De introductie van GreenMail en de nieuwe flag vereist documentatie en onderhoud, maar dit is minimaal. |
| **Complexiteit** | De complexiteit van de codebase neemt licht toe, maar dit wordt gecompenseerd door de verbeterde testbaarheid. |

---

## **5. Benodigde Wijzigingen**
| Bestand | Wijziging |
|---------|-----------|
| `pom.xml` | Voeg GreenMail toe als test-dependency. |
| `src/test/resources/application.properties` | Configureer GreenMail voor tests. |
| `StationEndToEndTests.java` | Implementeer GreenMail in de tests met validatie en reset-logica. |
| `EmailSenderService.java` | Voeg een flag toe om e-mails te skippen als `USE_REAL_MAILSERVER=false` en voeg logging toe. |
| `src/main/resources/application.properties` | Voeg de flag `use.real.mailserver` toe met default waarden. |
| `README.md` | Documenteer de flag en hoe ontwikkelaars/tests kunnen schakelen tussen een echte mailserver en GreenMail. |

---

## **6. Validatie**
- **Unit tests**: Controleren of `EmailSenderService` correct omgaat met de flag.
- **End-to-end tests**: Controleren of GreenMail correct werkt en of de tests slagen zonder een echte mailserver.
- **Integratietests**: Controleren of de echte mailserver nog steeds werkt als `USE_REAL_MAILSERVER=true`.

---

## **7. Conclusie**
Dit plan biedt een **robuuste, veilige en onderhoudbare** oplossing voor het probleem met de mailserver in de tests. Alle **risico's** zijn geïdentificeerd en **mitigaties** zijn geïntegreerd in het plan. De oplossing:
- Maakt het mogelijk om tests **zonder een echte mailserver** uit te voeren.
- Behoudt de functionaliteit **met een echte mailserver**.
- Is **veilig** en heeft **geen impact op productie**.
- Is **goed gedocumenteerd** en **onderhoudbaar**.

Laat me weten als je akkoord gaat met dit plan of als je aanvullende wijzigingen wilt! **Toggle naar Act mode** om de wijzigingen door te voeren.