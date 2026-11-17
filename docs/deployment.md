# Deployment: OTAP-opzet met Docker en Dokploy

Dit document beschrijft hoe de backend wordt uitgerold (issues #171, #172, #173, #178) en wat handmatig in Dokploy (https://panel.noelp.dev) moet gebeuren. Niets hiervan wordt door de code of workflows zelf aangemaakt: de omgevingen moeten door een beheerder in Dokploy worden ingericht.

## Overzicht

| Omgeving | Doel | Branch / trigger | Image-tag | Goedkeuring |
|----------|------|------------------|-----------|-------------|
| Lokaal | Ontwikkelen | n.v.t. (`docker-compose.yml`) | lokaal gebouwd | nee |
| Test | Automatisch de laatste ontwikkelversie | push naar `s3nj26_dev` | `test` | nee |
| Acceptatie | Opdrachtgever keurt goed | handmatig (workflow_dispatch) | `acceptance` | aanbevolen |
| Productie | Echte gebruikers | handmatig (workflow_dispatch) | `production` | verplicht (reviewer) |

Principe: **een keer bouwen, daarna promoten**. Het image wordt eenmalig gebouwd en getagd met de commit (`sha-1a2b3c4`). Naar acceptatie of productie gaat exact hetzelfde image (alleen een extra tag), alleen de omgevingsvariabelen verschillen. Zo test je wat je uiteindelijk uitrolt.

## Bestanden

| Bestand | Rol |
|---------|-----|
| `ClimateChecker/Dockerfile` | Multi-stage image (zie issue #174) |
| `ClimateChecker/docker-compose.yml` | Lokaal, frontend met hot reload |
| `ClimateChecker/docker-compose-dev.yml` | Lokaal, frontend zonder hot reload |
| `ClimateChecker/docker-compose-prod.yml` | Test, acceptatie en productie (image uit GHCR) |
| `ClimateChecker/.env.example` | Alle variabelen, zonder echte waarden |
| `.github/workflows/ci.yml` | Bouwen en testen |
| `.github/workflows/deploy.yml` | Image naar GHCR en Dokploy-webhook |

`docker-compose-prod.yml` bevat geen secrets, heeft healthchecks en `restart: unless-stopped`, publiceert de database-poort niet en gebruikt een benoemd volume (`db_data`) zodat data een herstart of nieuwe container overleeft.

## Pijplijn

1. Push naar `s3nj26_dev` start `ci.yml` (tests) en `deploy.yml`.
2. `deploy.yml` bouwt het image, pusht `ghcr.io/ontdekit/climatechecker-backend:sha-<commit>` en zet de tag `test`.
3. De Dokploy-webhook `DOKPLOY_TEST_WEBHOOK` wordt aangeroepen, Dokploy haalt `:test` binnen en herstart de applicatie.
4. Promoveren: Actions, workflow "Deploy", Run workflow, kies `acceptance` of `production` en vul de `source_tag` in (bijv. `sha-1a2b3c4`, dezelfde als die op test goed draaide).

Is een webhook-secret niet gezet, dan slaat de workflow de deploy-stap over (de run blijft groen). De deploy op test loopt parallel aan CI en wacht er niet op; draai acceptatie en productie dus alleen met een tag waarvan CI groen was.

## Handmatig in GitHub

1. Repository settings, Environments: maak `test`, `acceptance` en `production` aan.
2. Zet bij `acceptance` en vooral `production` "Required reviewers".
3. Zet per environment het secret: `DOKPLOY_TEST_WEBHOOK`, `DOKPLOY_ACCEPTANCE_WEBHOOK`, `DOKPLOY_PRODUCTION_WEBHOOK` (de "Webhook URL" uit Dokploy, zie hieronder).
4. Controleer dat het GHCR-pakket `climatechecker-backend` leesbaar is voor de server (of geef Dokploy een registry-login, zie hieronder).

## Handmatig in Dokploy (per omgeving)

Herhaal voor test, acceptatie en productie, elk als eigen project (bijv. `climatechecker-test`). Kies een actieve server die voor dit project is afgesproken.

1. **Project** aanmaken.
2. **Database**: MariaDB 11.2 (niet nieuwer, Flyway 10.10 ondersteunt dat niet). Database-naam moet `ontdekstation013` zijn (`spring.flyway.schemas` staat daarop vast). Zet een sterk wachtwoord, schakel **backups** in (B2) en test een restore voordat je productie gebruikt.
3. **Application** voor de backend, Source type Docker: image `ghcr.io/ontdekit/climatechecker-backend:<omgeving>` (tag `test`, `acceptance` of `production`). Voeg een registry toe als het pakket privaat is (GitHub PAT met `read:packages`).
4. **Environment variabelen** zetten (zie `ClimateChecker/.env.example`). Per omgeving verplicht uniek:
   - `JWT_SECRET` en `TRUSTED_IP_PEPPER` (`openssl rand -base64 48`), nooit hetzelfde als in een andere omgeving. Zonder deze start de backend niet.
   - `SPRING_DATASOURCE_URL` (`jdbc:mariadb://<db-host>:3306/ontdekstation013`), `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - `ROOT_USER_*`, `MAIL_*`, `MAILSERVER_RELAY_*`, `USE_REAL_MAILSERVER` (zie `docs/email-self-hosting.md`)
   - `FRONTEND_HOST` (publieke URL van de frontend, gebruikt in mails) en `CORS_ALLOWED_ORIGINS` (dezelfde URL; meerdere gescheiden door komma's). Zonder deze blokkeert CORS de frontend.
5. **Domein** koppelen (bijv. `api.test.<domein>`) met HTTPS (Let's Encrypt), poort 8082.
6. **Healthcheck** instellen op `/actuator/health/readiness` (zie `docs/monitoring.md`).
7. **Deploy-webhook**: kopieer de webhook-URL van de applicatie en sla die op als GitHub-secret (zie hierboven). Behandel de URL als geheim.
8. Frontend: eigen Application met image `ghcr.io/ontdekit/climatechecker-frontend:<tag>`. Let op: `VITE_BACKEND_API_URL` wordt in de frontend bij het bouwen vastgelegd, dus een image werkt maar voor een backend-URL. Voor drie omgevingen is dus per omgeving een eigen frontend-build nodig (aan te passen in Software-FrontEnd).
9. Optioneel: gebruik `docker-compose-prod.yml` als Dokploy "Compose"-service i.p.v. losse applicaties. Zet dan dezelfde variabelen plus `BACKEND_TAG`.

## Omgevingsverschillen in een oogopslag

| Variabele | Test | Acceptatie | Productie |
|-----------|------|------------|-----------|
| `BACKEND_TAG` | `test` | `acceptance` | `production` |
| `JWT_SECRET`, `TRUSTED_IP_PEPPER`, DB-wachtwoord | uniek | uniek | uniek |
| `FRONTEND_HOST`, `CORS_ALLOWED_ORIGINS` | test-URL | acceptatie-URL | productie-URL |
| `USE_REAL_MAILSERVER` | `false` of testrelay | `true` | `true` |
| Backups database | optioneel | aan | aan, restore getest |

## Rollback

Zet de omgevingstag terug op een eerder image en roep de webhook aan: draai "Deploy" met `source_tag` van de vorige goede versie. Database-migraties (Flyway) worden niet automatisch teruggedraaid.

## Risico's en aandachtspunten

- De Dokploy-webhook is niet in een echte omgeving getest (geen Dokploy-toegang gebruikt bij het schrijven). Controleer bij de eerste run of een POST zonder body volstaat voor het bron-type "Docker".
- `StationController` heeft nog een hardcoded `@CrossOrigin` voor `http://localhost:5173`; de globale CORS-config (`CORS_ALLOWED_ORIGINS`) dekt de rest, maar die annotatie is overbodig.
- `spring.flyway.schemas` is hardcoded op `ontdekstation013`.
