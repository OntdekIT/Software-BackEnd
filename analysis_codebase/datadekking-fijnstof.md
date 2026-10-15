# Datadekking fijnstofmeetstations: analyse- en adviesnotitie

> Hoort bij user story Software-FrontEnd#120: "Als gebruiker wil ik weten waarom we wisselend data ontvangen van de fijnstofmeetstations".
>
> **Status: concept.** Hoofdstuk 2 gaat over oorzaken die in de code zelf zitten en is nagelopen in de broncode. Hoofdstuk 3 (bevindingen uit de data) moet nog worden ingevuld met echte cijfers van de nieuwe pagina *Beheer → Datadekking*. Tijdens het bouwen was meetjestad.net niet bereikbaar, dus er zijn nog geen echte meetgegevens geanalyseerd.

## 1. Wat er gebouwd is

| Onderdeel | Waar | Wat het doet |
|---|---|---|
| `DataCoverageLogic` | backend `utility/` | Deelt een periode op in uren of dagen en telt per station in hoeveel van die perioden er minstens één PM2.5/PM10-meting binnenkwam. |
| `DataCoverageService` + `GET /api/measurement/coverage` | backend | Haalt de ruwe metingen (ook "foute" waarden) van alle bekende stations op bij MeetJeStad en geeft de dekking terug. Alleen voor admins; maximaal 31 dagen. |
| Pagina *Datadekking* | frontend `/admin/data-coverage` | Samenvatting, grafiek van het meetnet over tijd, lijst van gelijktijdige uitval en een tijdlijn per station. |

**Definities**

- **Dekkingsgraad station** = gedekte perioden ÷ alle perioden. Een periode is gedekt als er minstens één fijnstofmeting in zit.
- **Status per station:**
  - *Betrouwbaar*: dekking ≥ 80%.
  - *Onregelmatig*: wel fijnstof, maar dekking < 80%.
  - *Geen fijnstof*: wel metingen, maar nooit een PM-waarde.
  - *Geen data*: helemaal niets ontvangen.
- **Dekking meetnet** = per periode het aandeel van de stations met fijnstof dat data stuurde, gemiddeld over de hele periode. Stations zonder fijnstofsensor tellen hier niet mee, anders lijkt het netwerk slechter dan het is.
- **Gelijktijdige uitval**: perioden waarin minder dan 50% van de stations met fijnstof data stuurde. Valt een groot deel van het netwerk tegelijk weg, dan ligt de oorzaak waarschijnlijk niet bij één sensor.

De drempels van 80% en 50% zijn een eerste keuze. Stem ze af met de opdrachtgever; ze staan als constanten in `DataCoverageLogic`.

## 2. Oorzaken in onze eigen code (nagelopen in de broncode)

1. **Metingen zonder geldige GPS-positie worden stil weggegooid.** `MeetJeStadService.getMeasurements()` houdt alleen metingen binnen de `cityLimits`-polygoon over. `MeasurementDto.latitude/longitude` zijn primitieve `float`s, dus een ontbrekende positie wordt 0,0. NaN valt ook buiten de polygoon. Zulke metingen verdwijnen, ook als er een geldige fijnstofwaarde in zat. Voor de gebruiker lijkt dat op ontbrekende data. Hierdoor kan `loc_error` in `StationMonitorService` in de praktijk ook bijna nooit afgaan: metingen zonder locatie bereiken die controle niet.
2. **Uitval is niet terug te zien.** `StationMonitorService` zet alleen de huidige stand van `is_active` en `stof_error`. Er wordt niet bijgehouden wanneer en hoe lang een station uitviel, dus achteraf is geen patroon te zien.
3. **`stof_error` kijkt naar 24 uur.** Een station krijgt pas `stof_error` als het 24 uur lang geen enkele fijnstofmeting stuurde. Korte of terugkerende gaten (een paar uur per dag) blijven daardoor onzichtbaar.
4. **"Geen sensor" en "kapotte sensor" lopen door elkaar.** De monitor maakt geen onderscheid tussen een station zonder fijnstofsensor en een station waarvan de sensor stuk is. Beide krijgen `stof_error`. De nieuwe pagina splitst dit wel (*Geen fijnstof* tegenover *Onregelmatig*).
5. **Wat géén oorzaak is:** de `MeasurementDeduplicator` voegt alleen dubbele rijen met hetzelfde station en tijdstip samen; er verdwijnen geen perioden door. `IncorrectValueFilter` raakt alleen temperatuur en luchtvochtigheid, niet fijnstof.

## 3. Bevindingen uit de data (nog invullen)

Open *Beheer → Datadekking*, kies "Laatste 30 dagen" + "Per dag" voor het overzicht en "Laatste 7 dagen" + "Per uur" voor details. Vul in:

| Vraag | Bevinding |
|---|---|
| Dekking meetnet over 30 dagen | _…%_ |
| Aantal stations per status (betrouwbaar / onregelmatig / geen fijnstof / geen data) | _… / … / … / …_ |
| Gelijktijdige uitval: hoe vaak en hoe lang? Vallen ze op vaste tijden (bijv. 's nachts, rond een deploy)? | _…_ |
| Typisch meetinterval (kolom *Interval*) en of dat per station verschilt | _…_ |
| Stations met terugkerende korte gaten tegenover één lange uitval | _…_ |
| Stations die in de lijst ontbreken of als *Geen data* staan terwijl ze wel online lijken (wijst op het GPS-punt uit §2.1) | _…_ |

## 4. Advies

1. **Metingen zonder GPS niet weggooien maar meetellen**, bijvoorbeeld door de laatst bekende positie van het station te gebruiken. Dat is de oorzaak met de meeste impact die we zelf kunnen oplossen (§2.1). Leg het eerst met de cijfers uit §3 vast.
2. **Statuswissels opslaan** (een tabel met station, type, tijdstip en nieuwe stand) in `StationMonitorService`. Dan wordt uitval ook buiten MeetJeStad om terug te zien en is de dekking niet afhankelijk van een live-aanroep.
3. **`stof_error` op een korter venster baseren** of op de dekkingsgraad, zodat terugkerende gaten ook een melding geven.
4. **"Geen fijnstofsensor" als eigenschap van het station vastleggen**, zodat deze stations geen onterechte foutmeldingen krijgen.
5. **Drempels (80% / 50%) afstemmen** met de opdrachtgever.
6. Valt uit §3 vooral gelijktijdige uitval op, **meld dit dan bij Meet je Stad.** Het is dan waarschijnlijk geen probleem in onze code of bij losse deelnemers.

## 5. Beperkingen

- De pagina vraagt de data live op bij MeetJeStad. Een periode van 30 dagen kan daardoor traag zijn; de backend heeft een read-timeout van 30 seconden. Zie advies 2 voor een structurele oplossing.
- Alleen stations die de synchronisatie ooit in de database heeft gezet worden opgevraagd. Een station dat nog nooit binnen de stadsgrens data stuurde komt niet in het overzicht.
- Het verwachte meetinterval per sensor is niet bekend. Daarom telt de dekking perioden (uur of dag) en geen losse metingen.
