# MB Ontdekt

### Introductie

Ontdekstation013 is een organisatie in Tilburg waar jongeren hun talenten kunnen ontdekken op gebied van wetenschap en techniek. In samenwerking met Bibliotheek Midden-Brabant en de gemeente Tilburg hebben zij een project opgezet rondom het meten van luchtkwaliteit in de regio Tilburg. In de regio staan meerdere meetstations om verschillende waardes op te meten. Op het moment gaat dit over temperatuur en luchtvochtigheid. Waar dit project tot stand is gekomen is bij de wens om deze gegevens eenvoudig inzichtelijk te maken voor het publiek. Ontdekstation013 geeft workshops om meetstations te maken en de data hiervan wordt automatisch naar MeetJeStad gestuurd. Wij halen deze data vervolgens weer op om deze vervolgens op een nette manier online weer te geven.

### Opstart instructies

**Backend**

- Zorg dat je een mariadb database draaiende hebt en dat je hierbij de database dump geïmporteerd hebt. Deze is te vinden in de documentatie repository
- Kopieer `ClimateChecker/src/main/resources/secrets.properties.example` en plak deze in dezelfde map, maar dan zonder de .example extensie. Vul hierin de database gegevens in. Deze worden automatisch geïmporteerd naar `application.properties` om te voorkomen dat je gegevens in Git staan.
- Build en run de backend in IntelliJ
  - Indien je dit handmatig doet: `mvn package --file pom.xml -DskipTests`

**Frontend**
- Download de dependencies van de frontend: `npm install`
- Build de frontend: `npm start`

### Gebruikte technologieën
- Backend
  - Java
  - Springboot
  - Lombok
  - Gson
- Frontend
  - React.JS
  - Cypress

Daarnaast gebruiken wij een externe API van [MeetJeStad](https://meetjestad.net/data/) om de data op te halen en gebruiken wij als database een MariaDB database.
