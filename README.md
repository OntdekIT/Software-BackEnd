# MB Ontdekt

### Introductie

Ontdekstation organiseert samen met Bibliotheek Midden-Brabant workshops voor jong en oud die hun talenten kunnen ontdekken op het gebied van wetenschap en techniek. Er is een project opgezet om de temperatuur en luchtkwaliteit te meten. Er worden workshops gegeven voor het maken van een meetstation. Een meetstation verzamelt data en verstuurt die naar 'Meet je Stad'. Deze web applicatie haalt de data weer op van de API van Meet je Stad. De gegevens worden overzichtelijk weergegeven op de home pagina van de MB-Ontdekt applicatie.

### Opstart instructies

Voor meer informatie over het opzetten van het project wordt doorverwezen naar de wiki. Bekijk de [Getting Started](https://github.com/OntdekIT/Software-Documents/wiki/Getting-Started) page om zowel de backend als frontend te configureren.

#### Snelle Windows-setup met `setup_backend.bat`

In de projectroot staat `setup_backend.bat`. Dit script helpt je om zonder Docker een lokale backend-omgeving op te zetten via een menu:

1. **Optie 5** — genereer een `.env` bestand met de standaard localhost-configuratie.
2. **Optie 2** — installeer de runtime tooling (Java 21, Maven, Node LTS) via [MISE](https://mise.jdx.dev).
3. **Optie 3 / 4** — installeer en start MariaDB (vereist Chocolatey of een handmatige installatie).
4. **Optie 1** — bouw en start de Spring Boot backend op `http://localhost:8082`.

Alle runtime-instellingen (database, mail, root-gebruiker) worden uit `.env` gelezen. Pas `.env` aan voor je eigen omgeving.

### Mailserver configuratie
De backend kan e-mails versturen via een echte SMTP-server of de verzending onderdrukken (bijv. voor tests).

| Variabele / property | Standaardwaarde | Beschrijving |
|----------------------|-----------------|--------------|
| `USE_REAL_MAILSERVER` / `use.real.mailserver` | `false` | Op `true` zetten om echt e-mails te versturen; `false` slaat verzending over en logt een waarschuwing. |
| `MAILSERVER_RELAY_HOST` | `localhost` | Hostname van de SMTP-relay. |
| `MAILSERVER_RELAY_PORT` | `3025` | Poort van de SMTP-relay. |
| `MAIL_USERNAME` | `test@localhost` | Gebruikersnaam voor SMTP-authenticatie. |
| `MAIL_PASSWORD` | `test` | Wachtwoord voor SMTP-authenticatie. |

**Productie:** zet `USE_REAL_MAILSERVER=true` en vul de overige mail-variabelen in.

**Tests:** de test-suite draait zonder echte mailserver. Voor end-to-end tests wordt automatisch een in-memory GreenMail SMTP-server opgestart; unit tests gebruiken Mockito voor de mail-afhankelijkheden.
