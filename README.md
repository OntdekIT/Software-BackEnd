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

### Overige standaardwaarden en proxies

Naast `JWT_SECRET` weigert de backend buiten het profiel `dev` of `test` ook te starten met `TRUSTED_IP_PEPPER` op de bekende standaardwaarde (of leeg), met `SPRING_DATASOURCE_PASSWORD` of `ROOT_USER_PASSWORD` op `password` of de placeholder `change-me` uit `.env.example`. Zet per omgeving eigen, unieke waarden.

`X-Forwarded-For` wordt alleen gelezen als het verzoek van een bekende proxy komt (rate limiting en vertrouwde IP's). Standaard zijn dat loopback en de privéreeksen (`10/8`, `172.16/12`, `192.168/16`), dus een reverse proxy in het Docker-netwerk werkt zonder extra instelling. Wil je het aanscherpen, zet dan `TRUSTED_PROXIES` op een kommagescheiden lijst met IP's of CIDR-reeksen. Een client die rechtstreeks verbindt kan de header niet meer vervalsen.

### API-documentatie (OpenAPI / Swagger UI)

De backend genereert zijn eigen API-documentatie uit de code (springdoc-openapi). Alle controllers staan erin, met Nederlandse samenvattingen, request- en responsebodies en foutcodes.

| Wat | URL (lokaal) |
|-----|--------------|
| Swagger UI | `http://localhost:8082/swagger-ui.html` |
| OpenAPI-specificatie (JSON) | `http://localhost:8082/v3/api-docs` |

- Alleen beschikbaar met het profiel `dev` of `test` (`application-dev.properties` en `application-test.properties`). Zonder die profielen, dus ook in productie, staat springdoc uit en zijn de paden dicht (`springdoc.api-docs.enabled=false`).
- Beveiligde endpoints proberen: log in via `POST /api/authentication/login` (en `verify`), klik in Swagger UI op "Authorize" en plak het JWT. Het token gaat mee als `Authorization: Bearer <token>`, cookies worden niet gebruikt.
- Een test (`OpenApiDocsTest`) controleert dat de specificatie gegenereerd wordt.

#### Padnamen, aliassen en Postman

- Alle paden zijn kleine letters, bijvoorbeeld `/api/meetstation/...`, `/api/meetstation/claim`, `/api/meetstation/availability/{stationId}/{workshopCode}` en `/api/meetstation/stations-met-measurements`.
- De oude spellingen (`/api/Meetstation/...`, `/Claim`, `/Availibility/...`, `/stationsMetMeasurements`) blijven werken als alias zodat de frontend niet breekt, maar staan niet in de specificatie. Een nieuw pad voor een station? Zet het in de beveiligingsregels van `SecurityConfiguration` voor beide spellingen: padmatching is hoofdlettergevoelig. `PathAliasApiTest` controleert dat elk muterend stationspad om een login vraagt.
- De specificatie staat ingecheckt in `docs/api/openapi.json` en `OpenApiSpecTest` faalt als code en bestand uit elkaar lopen. Verversen na een wijziging in de controllers: `cd ClimateChecker && ./mvnw test -Dtest=OpenApiSpecTest -Dopenapi.update=true`.
- De Postman-collectie `postman/ClimateChecker.postman_collection.json` wordt daaruit gegenereerd met `./scripts/generate-postman.sh` (vereist Node.js; importeer het bestand in Postman). Zet de collectievariabele `baseUrl` (standaard `http://localhost:8082`) en plak je JWT in `bearerToken`.

### Dataflow

Een verzoek van de app roept meetjestad.net nooit aan. Alle werk gebeurt op de achtergrond, de app leest alleen voorberekende gegevens.

```
meetjestad.net --(IngestWorker, elke 5 min, incrementeel)--> database --(na elke run)--> snapshots (geheugen) --> endpoints
```

**Ingest-worker.** De `IngestWorker` is de enige aanroeper van meetjestad.net. Hij haalt periodiek alleen metingen op sinds de vorige geslaagde run (plus een overlap van 10 minuten), slaat ze idempotent op (sleutel: station + tijdstip, dus geen dubbelen bij overlap of herstart) en ververst daarna de snapshots. Runs overlappen nooit (single-flight). Hij houdt een minimale tussentijd en een maximum per uur aan, en wacht na een fout (429, 5xx, time-out) exponentieel langer met jitter; een `Retry-After` van de bron wint als die langer is. De bron publiceert geen vast limiet, daarom zijn de defaults voorzichtig. Na een herstart gaat hij verder vanaf de nieuwste meting in de database (maximaal 7 dagen terug).

**Snapshots.** Na elke ingest-run (ook na een mislukte, zodat de status klopt) worden in een keer, atomair, opnieuw berekend: de stations met laatste meting (`/api/meetstation/stations-met-measurements`), de wijken voor "nu" (`/api/neighbourhood/history`), de time-lapse over 24 uur met 24 en 25 stappen (`/api/neighbourhood/history/timelapse`) en de geconsolideerde staat (`/api/snapshot`). Een verzoek voor "nu" (binnen `snapshot.now-tolerance-seconds`) of een standaard time-lapse wordt hieruit beantwoord. Een ander tijdstip wordt uit de lokale database berekend en per minuut 60 s gecachet (Caffeine). Is er bij het opstarten nog geen snapshot, dan wordt hij uit de database opgebouwd; een lege database geeft lege lijsten, geen live-aanroep. De legacy-endpoints houden hun vorm en tijdnotatie (`dd-MM-yyyy HH:mm:ss`).

**Versheid.** Elk kaartantwoord heeft de header `X-Data-As-Of` (ISO-8601 UTC, tijdstip van de laatste geslaagde ingest), zichtbaar voor browsers via de CORS-exposed headers. `GET /api/status` toont de ingest-status (`lastSuccessAt`, `lastAttemptAt`, `lastError`, `consecutiveFailures`, `nextAttemptAt`, `stale`). De readiness-check (`/actuator/health/readiness`) geeft `DEGRADED` (HTTP 200, geen herstart) als de laatste geslaagde ingest ouder is dan `ingest.stale-after-minutes`.

**`GET /api/snapshot`.** Een compacte JSON met alles wat Kaart, Heatmap, Wijken en Dashboard nodig hebben, zodat ze het per constructie eens zijn. Alle tijden zijn ISO-8601 UTC, gemiddelden zijn `null` als er geen actuele waarde is (nooit NaN, nooit een oude waarde). Het formaat is stabiel: velden worden alleen toegevoegd (zie `schemaVersion`).

| Onderdeel | Inhoud |
| --- | --- |
| `dataAsOf`, `generatedAt`, `recencyMinutes`, `ingest` | versheid en status van de ingest |
| `wijken[]` | `id`, `name`, `avgTemp`, `avgPm25`, `stationCount`, `lastMeasurementAt`, `series[24]` (uurwaarden, `t` is het einde van het uur) |
| `city` | `avgTemp`, `avgPm25` (gemiddelde over wijken met actuele waarde), `warmest`, `coldest` (gelijk: op naam), `biggestChange` over 24 uur |
| `stations[]` | `id`, `name`, `latitude`, `longitude`, `isActive`, `latest{timestamp,temperature,humidity,pm25,pm10}`, `flags`, `status` (`ok` of `no_current_value`), `reason` (`uitval`, `sensorfout`, `late_meting`), `lastMeasurementAt` (ook voor offline stations) |
| `network` | `stationsTotal`, `active`, `offline`, `withSensorErrors`, `coveragePercent24h` (aandeel station-uren met data, over alle stations) |
| `heatmapPoints[]` | stations met actuele temperatuur: `latitude`, `longitude`, `temperature`, `pm25`, `ageMinutes` |

Recentieregel: een waarde is actueel als de meting hoogstens `snapshot.recency-minutes` (standaard 120) oud is. Per station en grootheid telt de nieuwste niet-lege waarde binnen die termijn. Een wijk met alleen een oudere waarde krijgt `null`. Het antwoord heeft een zwakke `ETag` (`If-None-Match` geeft 304) en `Cache-Control: public, max-age=30`, en wordt gzip-gecomprimeerd.

**Configuratie** (property of env-var, alle met een default):

| Property | Env-var | Default | Betekenis |
| --- | --- | --- | --- |
| `ingest.enabled` | `INGEST_ENABLED` | `true` | kill switch: uit betekent geen enkele externe aanroep |
| `ingest.interval-seconds` | `INGEST_INTERVAL_SECONDS` | `300` | tijd tussen runs |
| `ingest.overlap-minutes` | `INGEST_OVERLAP_MINUTES` | `10` | overlap met het vorige venster |
| `ingest.initial-window-hours` | `INGEST_INITIAL_WINDOW_HOURS` | `24` | eerste venster bij een lege database |
| `ingest.max-catch-up-hours` | `INGEST_MAX_CATCH_UP_HOURS` | `168` | maximale inhaalperiode na stilstand |
| `ingest.min-request-interval-seconds` | `INGEST_MIN_REQUEST_INTERVAL_SECONDS` | `60` | minimale tijd tussen verzoeken |
| `ingest.max-requests-per-hour` | `INGEST_MAX_REQUESTS_PER_HOUR` | `30` | maximum verzoeken per uur |
| `ingest.backoff-initial-seconds` / `ingest.backoff-max-seconds` | `INGEST_BACKOFF_INITIAL_SECONDS` / `INGEST_BACKOFF_MAX_SECONDS` | `30` / `1800` | backoff na fouten |
| `ingest.stale-after-minutes` | `INGEST_STALE_AFTER_MINUTES` | `30` | daarna is de readiness `DEGRADED` |
| `snapshot.recency-minutes` | `SNAPSHOT_RECENCY_MINUTES` | `120` | recentieregel |
| `snapshot.now-tolerance-seconds` | `SNAPSHOT_NOW_TOLERANCE_SECONDS` | `300` | marge voor "nu" |
| `snapshot.cache-max-age-seconds` | `SNAPSHOT_CACHE_MAX_AGE_SECONDS` | `30` | `max-age` van `/api/snapshot` |
| `map.cache.ttl-seconds` / `map.cache.max-entries` / `map.cache.timelapse-max-entries` | `MAP_CACHE_TTL_SECONDS` / `MAP_CACHE_MAX_ENTRIES` / `MAP_CACHE_TIMELAPSE_MAX_ENTRIES` | `60` / `120` / `20` | cache voor andere tijdstippen |

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
