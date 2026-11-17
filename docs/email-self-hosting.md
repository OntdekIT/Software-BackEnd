# E-mailserver in eigen beheer: onderzoek en ontwerp

Status: onderzoek, er is nog niets opgezet (issue #179). Doel: de uitgaande e-mail van de app (registratie- en inlogcodes, wachtwoordreset) niet meer van een externe dienst of een persoonlijk Gmail-account laten afhangen.

## 1. Wat de app nu doet

- Mail gaat via Spring `JavaMailSender` (SMTP). Configuratie komt uit environment variabelen:

| Variabele | Property | Default |
|-----------|----------|---------|
| `USE_REAL_MAILSERVER` | `use.real.mailserver` | `false` (verzenden wordt overgeslagen) |
| `MAILSERVER_RELAY_HOST` | `spring.mail.host` | `localhost` |
| `MAILSERVER_RELAY_PORT` | `spring.mail.port` | `3025` |
| `MAIL_USERNAME` | `spring.mail.username` | `test@localhost` |
| `MAIL_PASSWORD` | `spring.mail.password` | `test` |

- `mail.smtp.starttls.enable=true` staat vast aan; `mail.smtp.auth` staat niet in `application.properties`.
- De afzender is in `EmailSenderService` op meerdere plekken hardcoded op persoonlijke Gmail-adressen (`setFrom`).
- Er is alleen **uitgaande** mail nodig (geen mailboxen voor mensen, geen inkomende mail). Dat maakt de opzet veel simpeler dan een volledige mailserver.

## 2. Opties

### A. Volledige mailserver: docker-mailserver of Mailu
- **docker-mailserver** (`mailserver/docker-mailserver`): een container met Postfix, Dovecot, DKIM (opendkim), SPF/DMARC-checks, Fail2ban. Configuratie via bestanden en een CLI (`setup`). Staat al in onze compose-bestanden (alleen lokaal, zonder echte configuratie).
- **Mailu**: vergelijkbaar, met webinterface voor beheer en webmail. Zwaarder (meerdere containers).
- Voordelen: volledige controle, geen kosten per mail, mailboxen mogelijk.
- Nadelen: de server moet een goede reputatie opbouwen; onderhoud (updates, beveiliging, opslag); mail van een nieuw IP komt vaak in spam; veel providers blokkeren uitgaande poort 25.

### B. Alleen een SMTP-relay met eigen domein (aanbevolen)
- Een kleine Postfix-container (of docker-mailserver met alleen verzenden) die via een externe SMTP-relay met goede reputatie verstuurt (bijv. Brevo, Mailgun, Amazon SES, Scaleway TEM). Of direct de SMTP van zo'n dienst gebruiken met het eigen domein en eigen DKIM-sleutel.
- Voordelen: eigen domein, maar de afleverbaarheid komt van de relay. Weinig onderhoud.
- Nadelen: nog steeds een externe afhankelijkheid en (kleine) kosten bij volume.

### C. Via Dokploy
- Dokploy host zelf geen mailserver. Je kunt docker-mailserver of Mailu wel als Compose-service in Dokploy draaien (zelfde serverbeheer, backups en domeinen als de rest) of de relay-container uit B.
- Let op: de mailserver hoort op een server met een vast, "schoon" IP en niet op een server waar andere workloads de reputatie kunnen schaden.

### Vergelijking

| | A. Volledig | B. Relay | Huidig (extern) |
|---|---|---|---|
| Beheerlast | hoog | laag | geen |
| Afleverbaarheid | moeilijk | goed | goed |
| Kosten | server + tijd | gratis tot laag | gratis tot laag |
| Eigen domein/DKIM | ja | ja | nee |
| Afhankelijk van derde | nee | ja (relay) | ja |

Aanbeveling: optie B voor productie. Optie A alleen als "in eigen beheer" een harde eis is en er iemand is die de reputatie en updates bewaakt.

## 3. DNS-eisen (voor elke optie met eigen domein)

Voorbeeld: `mail.example.org` als mailhost, `example.org` als afzenderdomein.

| Record | Waarde (voorbeeld) | Doel |
|--------|--------------------|------|
| `A` en `AAAA` voor `mail.example.org` | IP van de server | Mailhost |
| **PTR** (reverse DNS) van het server-IP | `mail.example.org` | Moet kloppen, anders wordt mail geweigerd. Instellen bij de hostingprovider |
| **SPF** `TXT` op `example.org` | `v=spf1 ip4:<server-ip> include:<relay> -all` | Wie mag namens het domein mailen |
| **DKIM** `TXT` op `<selector>._domainkey.example.org` | publieke sleutel van de server of relay | Handtekening op elke mail |
| **DMARC** `TXT` op `_dmarc.example.org` | `v=DMARC1; p=none; rua=mailto:dmarc@example.org` | Beleid en rapportage; start met `p=none`, daarna `quarantine` of `reject` |
| `MX` op `example.org` | alleen nodig als er ook mail ontvangen wordt (bounces, antwoorden) | |

Controle: SPF/DMARC/DKIM controleren met DNS-tools of mail-tester.com. Test eerst met laag volume en kijk of mail in spam belandt.

## 4. Kosten en risico's

- Kosten: server of VM (vanaf enkele euro's per maand), tijd voor beheer; relays zijn vaak gratis tot enkele duizenden mails per maand.
- **Poort 25** uitgaand is bij veel providers dicht; vraag het na voordat je kiest (een relay op poort 587 omzeilt dit).
- **Reputatie**: een nieuw IP staat standaard laag aangeschreven. Warm op met laag volume en controleer blacklists (Spamhaus).
- **Beveiliging**: een open relay of gelekte SMTP-credentials leidt snel tot misbruik en blacklisting. Geen onnodige poorten open, sterke wachtwoorden, Fail2ban, TLS verplicht.
- **Continuiteit**: een mailserver is een single point of failure; zonder werkende mail kunnen gebruikers niet inloggen (login- en signupcodes gaan per mail). Plan monitoring (queue, bounces) en een fallback.
- **Back-ups**: bij docker-mailserver horen de data- en config-volumes in de back-ups.
- **Privacy (AVG)**: mail bevat persoonsgegevens; leg vast wie toegang heeft en hoe lang logs bewaard worden.

## 5. Omschakelen in de app (alleen configuratie)

Voor optie A of B hoeft er voor het verbinden niets in de code te veranderen. Zet per omgeving (zie `docs/deployment.md`):

```
USE_REAL_MAILSERVER=true
MAILSERVER_RELAY_HOST=mail.example.org
MAILSERVER_RELAY_PORT=587
MAIL_USERNAME=<smtp-gebruiker>
MAIL_PASSWORD=<smtp-wachtwoord>
```

Spring-properties zonder eigen env-variabele zijn via "relaxed binding" te zetten, bijvoorbeeld `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true`.

Poort 587 gebruikt STARTTLS (staat al aan). Poort 465 vereist `SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE=true` en `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=false`. Test het omschakelen eerst op de testomgeving.

### Aanpassingen in de code die nodig zijn (apart issue)
1. De afzender is hardcoded op Gmail-adressen in `EmailSenderService`. Een `From` op een domein dat niet bij de eigen server of relay hoort laat SPF/DMARC falen en de mail in spam belanden. Maak de afzender configureerbaar (bijv. `mail.from` met env `MAIL_FROM`, een adres op het eigen domein) en gebruik die overal.
2. Maak `spring.mail.properties.mail.smtp.auth` configureerbaar (default `true`) in `application.properties`.
3. Op meerdere plekken staat een overbodige `helper.setTo("kayletmail@host.com")` die direct daarna overschreven wordt (dode code).

## 6. Voorgestelde vervolgstappen

1. Besluit: optie B (relay) of A (volledig), en welk domein de afzender wordt.
2. Kies een server die voor mail is afgesproken en controleer poort 25/587 en de PTR-mogelijkheid.
3. Stel DNS in (sectie 3) en test met mail-tester.com.
4. Doe de code-aanpassingen uit sectie 5.
5. Rol uit op test, daarna acceptatie en productie.
