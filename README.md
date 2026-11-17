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

### JWT_SECRET per omgeving

De backend tekent inlogtokens met `JWT_SECRET` (Base64, minimaal 256 bit). In `application.properties` staat een bekende standaard testsleutel als fallback voor je eigen laptop. De backend **start niet** als `JWT_SECRET` ontbreekt of die standaardsleutel is, tenzij het profiel `dev` of `test` actief is.

| Situatie | Wat te doen |
|----------|-------------|
| Lokaal ontwikkelen | Start met `SPRING_PROFILES_ACTIVE=dev` (docker-compose.yml en docker-compose-dev.yml doen dit al), of zet zelf een `JWT_SECRET`. |
| Unit-/integratietests | Het testprofiel staat aan in `src/test/resources/application.properties`. |
| Test, acceptatie, productie | Zet per omgeving een eigen, unieke `JWT_SECRET` (zie hieronder). Gebruik nooit hetzelfde geheim in twee omgevingen. |

Stappen per omgeving:

1. Genereer een sleutel: `openssl rand -base64 48`.
2. Zet hem als omgevingsvariabele `JWT_SECRET` in de omgeving zelf (bijv. Dokploy: Environment van de applicatie; docker-compose: `.env` naast het compose-bestand, niet in git).
3. Voor GitHub Actions (bijv. e2e-workflows) zet je hem als repository-secret (Settings, Secrets and variables, Actions) en geef je hem door via `${{ secrets.JWT_SECRET }}`.
4. Herstart de backend. Bestaande tokens worden daarmee ongeldig (gebruikers moeten opnieuw inloggen).
5. Rouleren: herhaal stap 1 t/m 4 per omgeving. Een lek? Direct roteren.

Gerelateerd (handmatig): de MailSlurp-key die ooit in de code stond moet in het MailSlurp-dashboard worden ingetrokken en vervangen, en de nieuwe key hoort alleen in GitHub-secrets en lokale `.env`-bestanden.

### API-documentatie (OpenAPI / Swagger UI)

De backend genereert zijn eigen API-documentatie uit de code (springdoc-openapi). Alle controllers staan erin, met Nederlandse samenvattingen, request- en responsebodies en foutcodes.

| Wat | URL (lokaal) |
|-----|--------------|
| Swagger UI | `http://localhost:8082/swagger-ui.html` |
| OpenAPI-specificatie (JSON) | `http://localhost:8082/v3/api-docs` |

- Alleen beschikbaar met het profiel `dev` of `test` (`application-dev.properties` en `application-test.properties`). Zonder die profielen, dus ook in productie, staat springdoc uit en zijn de paden dicht (`springdoc.api-docs.enabled=false`).
- Beveiligde endpoints proberen: log in via `POST /api/authentication/login` (en `verify`), klik in Swagger UI op "Authorize" en plak het JWT. Het token gaat mee als `Authorization: Bearer <token>`, cookies worden niet gebruikt.
- Een test (`OpenApiDocsTest`) controleert dat de specificatie gegenereerd wordt.

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
