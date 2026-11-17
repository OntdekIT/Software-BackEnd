# Healthcheck en monitoring

## Endpoints

De backend gebruikt Spring Boot Actuator. Alleen `health` is blootgesteld (`management.endpoints.web.exposure.include=health`), alle andere actuator-endpoints (env, beans, mappings, ...) zijn uit.

| Endpoint | Betekenis | Controleert de database? |
|----------|-----------|--------------------------|
| `GET /actuator/health` | Totaalstatus | ja |
| `GET /actuator/health/liveness` | Proces draait (herstart nodig als dit faalt) | nee |
| `GET /actuator/health/readiness` | Klaar voor verkeer | ja (`db`) |

Alle drie zijn publiek bereikbaar (zonder login, zie `SecurityConfiguration`) maar tonen geen details: het antwoord is alleen `{"status":"UP"}` of `{"status":"DOWN"}` met HTTP 200 of 503. Zo lekt er geen informatie over de database of andere componenten.

De mail-healthcheck staat bewust uit (`management.health.mail.enabled=false`): een onbereikbare mailserver mag de hele app niet als DOWN markeren en een herstart van de container veroorzaken.

## Gebruik

- Docker: de `HEALTHCHECK` in de Dockerfile (issue #174) en de `healthcheck` van de backend in docker-compose (issue #178) gebruiken `/actuator/health/readiness`.
- Dokploy: stel bij de applicatie een healthcheck in op `/actuator/health/readiness` (poort 8082).
- Extern (bv. Beszel, Uptime Kuma of de `uptime-check`): poll `/actuator/health` en alarmeer op een niet-200 antwoord.

Lokaal controleren:

```
curl -s http://localhost:8082/actuator/health
curl -s http://localhost:8082/actuator/health/readiness
```

## Eigen configuratie

Alle instellingen staan in `ClimateChecker/src/main/resources/application.properties` onder `management.*`.
