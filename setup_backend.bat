@echo off
setlocal EnableDelayedExpansion

:: ============================================================
::  MB-Ontdekt / ClimateChecker - Backend Setup Script
::  Doel: Een kale Windows-machine omzetten naar een werkende
::        backend-ontwikkel/testomgeving zonder Docker.
::  Tooling: MISE wordt gebruikt voor Java, Maven en Node/npm.
:: ============================================================

title ClimateChecker Backend Setup

:: Basisinstellingen
set "PROJECT_ROOT=%~dp0"
set "BACKEND_DIR=%PROJECT_ROOT%ClimateChecker"
set "MISE_VERSION=2026.9.11"
set "MISE_DIR=%USERPROFILE%\.local\bin"
set "MISE_EXE=%MISE_DIR%\mise.exe"
set "MISE_URL=https://github.com/jdx/mise/releases/download/v%MISE_VERSION%/mise-v%MISE_VERSION%-windows-x64.exe"
set "BACKEND_PORT=8082"
set "SPRING_PROFILES_ACTIVE=default"

:: ============================================================
::  MENU
:: ============================================================
:MENU
cls
echo ============================================================
echo   ClimateChecker - Backend Setup Menu
echo ============================================================
echo.
echo   1) Start backend
echo   2) Installeer packages / runtime omgeving (MISE: Java, Maven, Node, optional npm)
echo   3) Installeer servers (MariaDB)
echo   4) Start servers (MariaDB)
echo   5) Maak .env bestand aan
echo   0) Afsluiten
echo.
echo ============================================================
set "CHOICE="
set /p CHOICE="Kies een optie: "
if not defined CHOICE set "CHOICE=0"

if "%CHOICE%"=="1" goto START_BACKEND
if "%CHOICE%"=="2" goto INSTALL_PACKAGES
if "%CHOICE%"=="3" goto INSTALL_SERVERS
if "%CHOICE%"=="4" goto START_SERVERS
if "%CHOICE%"=="5" goto CREATE_ENV
if "%CHOICE%"=="0" goto EXIT_SCRIPT

echo Ongeldige keuze. Probeer opnieuw.
pause
goto MENU

:: ============================================================
::  OPTIE 1: Start backend
:: ============================================================
:START_BACKEND
echo.
echo [INFO] Start backend wordt voorbereid...

if not exist "%BACKEND_DIR%\pom.xml" (
    echo [FOUT] Kan %BACKEND_DIR%\pom.xml niet vinden.
    echo [FOUT] Zorg dat dit script in de project-root staat.
    pause
    goto MENU
)

if exist "%PROJECT_ROOT%\.env" (
    echo [INFO] Laad omgevingsvariabelen uit .env...
    call :LOAD_ENV "%PROJECT_ROOT%\.env"

    :: Toon alle geladen configuratiepaden en waarden
    call :PRINT_CONFIG

    :: Voeg het pad naar de MariaDB bin-map toe aan PATH indien ingesteld
    if defined MARIADB_BIN_PATH (
        :: Verwijder eventuele trailing backslash zodat paden netjes worden gecombineerd
        if "!MARIADB_BIN_PATH:~-1!"=="\" set "MARIADB_BIN_PATH=!MARIADB_BIN_PATH:~0,-1!"
        if exist "!MARIADB_BIN_PATH!\mysql.exe" (
            echo [INFO] MariaDB bin-map toegevoegd aan PATH: !MARIADB_BIN_PATH!
            set "PATH=!MARIADB_BIN_PATH!;%PATH%"
        ) else (
            echo [WAARSCHUWING] MARIADB_BIN_PATH is ingesteld, maar '!MARIADB_BIN_PATH!\mysql.exe' bestaat niet.
            echo [WAARSCHUWING] Controleer het pad in .env.
        )
    )

    :: Valideer of het mysql-commando beschikbaar is
    echo [INFO] Zoek mysql executable...
    where mysql 2>nul
    mysql --version >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo [WAARSCHUWING] mysql-commando niet gevonden. De database kan niet automatisch worden gecontroleerd/aangemaakt.
    ) else (
        for /f "usebackq tokens=*" %%i in (`where mysql`) do echo [INFO] mysql executable gevonden: %%i
        echo [INFO] mysql-commando is beschikbaar.
    )
)

netstat -ano | findstr ":%BACKEND_PORT%" | findstr "LISTENING" >nul
if !ERRORLEVEL! equ 0 (
    echo [INFO] Backend lijkt al te draaien op poort %BACKEND_PORT%.
    pause
    goto MENU
)

if not exist "%MISE_EXE%" (
    echo [FOUT] MISE is niet geinstalleerd. Kies eerst optie 2.
    pause
    goto MENU
)

"%MISE_EXE%" install >nul 2>&1

"%MISE_EXE%" exec java -- java -version >nul 2>&1
if !ERRORLEVEL! neq 0 (
    echo [FOUT] Java is niet beschikbaar via mise. Kies eerst optie 2.
    pause
    goto MENU
)

"%MISE_EXE%" exec maven -- mvn -v >nul 2>&1
if !ERRORLEVEL! neq 0 (
    echo [FOUT] Maven is niet beschikbaar via mise. Kies eerst optie 2.
    pause
    goto MENU
)

powershell -Command "try { (New-Object Net.Sockets.TcpClient).Connect('localhost', 3306); } catch { exit 1; }" >nul 2>&1
if !ERRORLEVEL! neq 0 (
    echo [WAARSCHUWING] MariaDB lijkt niet bereikbaar op localhost:3306.
    echo [WAARSCHUWING] Kies optie 4 om de servers te starten, of controleer de .env-configuratie.
    pause
)

if exist "%PROJECT_ROOT%\.env" (
    echo [INFO] Zorg dat database '%SPRING_DATASOURCE_SCHEME%' bestaat...
    call :ENSURE_DATABASE
)

echo [INFO] Backend bouwen en starten met Maven...
echo [INFO] Werkdirectory: %BACKEND_DIR%
echo [INFO] MISE executable: %MISE_EXE%
echo [INFO] Maven commando: "%MISE_EXE%" exec maven -- mvn -f "%BACKEND_DIR%\pom.xml" spring-boot:run -Dspring-boot.run.profiles=%SPRING_PROFILES_ACTIVE%
cd /d "%BACKEND_DIR%"
set "SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%"
"%MISE_EXE%" exec maven -- mvn -f "%BACKEND_DIR%\pom.xml" spring-boot:run -Dspring-boot.run.profiles=%SPRING_PROFILES_ACTIVE%

if !ERRORLEVEL! neq 0 (
    echo [FOUT] Backend kon niet worden gestart. Controleer de foutmeldingen hierboven.
    pause
    goto MENU
)

goto MENU

:: ============================================================
::  OPTIE 2: Installeer packages / runtime omgeving via MISE
:: ============================================================
:INSTALL_PACKAGES
echo.
echo [INFO] Packages / runtime omgeving wordt geinstalleerd via MISE...

if not exist "%MISE_EXE%" (
    echo [INFO] MISE is niet gevonden. MISE wordt nu gedownload en geinstalleerd...
    if not exist "%MISE_DIR%" mkdir "%MISE_DIR%"
    powershell -Command "Invoke-WebRequest -Uri '%MISE_URL%' -OutFile '%MISE_EXE%' -UseBasicParsing" >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo [FOUT] Kon MISE niet downloaden. Controleer de internetverbinding.
        pause
        goto MENU
    )
    echo [INFO] MISE is geinstalleerd in %MISE_EXE%.
) else (
    echo [INFO] MISE is al geinstalleerd.
)

if not exist "%PROJECT_ROOT%\.tool-versions" (
    echo [INFO] .tool-versions bestand wordt aangemaakt...
    (
        echo java 21
        echo maven latest
        echo node lts
    ) > "%PROJECT_ROOT%\.tool-versions"
)

cd /d "%PROJECT_ROOT%"
echo [INFO] Installeer/actualiseer Java 21, Maven en Node LTS via mise...
echo [INFO] MISE executable: %MISE_EXE%
echo [INFO] .tool-versions pad: %PROJECT_ROOT%.tool-versions
echo [INFO] Command: "%MISE_EXE%" trust "%PROJECT_ROOT%\.tool-versions"
"%MISE_EXE%" trust "%PROJECT_ROOT%\.tool-versions" >nul 2>&1
echo [INFO] Command: "%MISE_EXE%" install --yes
"%MISE_EXE%" install --yes

if !ERRORLEVEL! neq 0 (
    echo [FOUT] Installatie via MISE is mislukt. Controleer de foutmeldingen hierboven.
    pause
    goto MENU
)

echo [INFO] Java versie:
echo [INFO] Command: "%MISE_EXE%" exec java -- java -version
"%MISE_EXE%" exec java -- java -version
echo.
echo [INFO] Maven versie:
echo [INFO] Command: "%MISE_EXE%" exec maven -- mvn -v
"%MISE_EXE%" exec maven -- mvn -v
echo.
echo [INFO] Node/npm versie:
echo [INFO] Command: "%MISE_EXE%" exec node -- node -v
"%MISE_EXE%" exec node -- node -v
echo [INFO] Command: "%MISE_EXE%" exec node -- npm -v
"%MISE_EXE%" exec node -- npm -v

echo.
echo [INFO] Installatie succesvol afgerond.
pause
goto MENU

:: ============================================================
::  OPTIE 3: Installeer servers (MariaDB)
:: ============================================================
:INSTALL_SERVERS
echo.
echo [INFO] Servers worden geinstalleerd...

echo [INFO] Controleer MariaDB installatie...
echo [INFO] Zoek mysql executable...
where mysql 2>nul
mysql --version >nul 2>&1
if !ERRORLEVEL! equ 0 (
    for /f "usebackq tokens=*" %%i in (`where mysql`) do echo [INFO] MariaDB/mysql executable gevonden: %%i
    echo [INFO] MariaDB is al geinstalleerd.
) else (
    echo [INFO] MariaDB is niet gevonden. Probeer te installeren via Chocolatey...
    where choco >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        echo [INFO] Command: choco install mariadb -y --no-progress
        choco install mariadb -y --no-progress
        if !ERRORLEVEL! neq 0 (
            echo [FOUT] Installatie van MariaDB via Chocolatey mislukt.
            echo [INFO] Installeer MariaDB handmatig vanaf https://mariadb.org/download/
        ) else (
            echo [INFO] MariaDB is geinstalleerd.
            echo [INFO] HERSTART de computer indien gevraagd, daarna kies je optie 4.
        )
    ) else (
        echo [FOUT] Chocolatey is niet geinstalleerd. MariaDB kan niet automatisch worden geinstalleerd.
        echo [INFO] Opties:
        echo   - Installeer Chocolatey vanaf https://chocolatey.org/install
        echo   - Installeer MariaDB handmatig vanaf https://mariadb.org/download/
        pause
        goto MENU
    )
)

echo [INFO] Voor de lokale mail-simulatie is geen aparte installatie nodig.
echo [INFO] Zorg ervoor dat USE_REAL_MAILSERVER=false staat in .env.

echo.
echo [INFO] Server installatie afgerond.
pause
goto MENU

:: ============================================================
::  OPTIE 4: Start servers (MariaDB)
:: ============================================================
:START_SERVERS
echo.
echo [INFO] Servers worden gestart...

echo [INFO] Start MariaDB service...
echo [INFO] Command: net start MariaDB
net start MariaDB >nul 2>&1
if !ERRORLEVEL! equ 0 (
    echo [INFO] MariaDB service is gestart.
) else (
    echo [INFO] Command: net start MySQL
    net start MySQL >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        echo [INFO] MySQL/MariaDB service is gestart.
    ) else (
        echo [WAARSCHUWING] Kon MariaDB service niet starten. Mogelijk draait deze al of is de servicenaam anders.
    )
)

set /a wait=5
:WAIT_MARIADB
powershell -Command "try { (New-Object Net.Sockets.TcpClient).Connect('localhost', 3306); exit 0; } catch { exit 1; }" >nul 2>&1
if !ERRORLEVEL! neq 0 (
    if !wait! gtr 0 (
        timeout /t 1 /nobreak >nul
        set /a wait-=1
        goto WAIT_MARIADB
    )
    echo [WAARSCHUWING] MariaDB is niet bereikbaar op localhost:3306 na 5 seconden.
) else (
    echo [INFO] MariaDB is bereikbaar op localhost:3306.
)

echo [INFO] Mail wordt lokaal onderdrukt zodra USE_REAL_MAILSERVER=false staat in .env.
echo [INFO] Er is dus geen aparte mailserver nodig voor lokale ontwikkeling.

echo.
echo [INFO] Servers start-procedure afgerond.
pause
goto MENU

:: ============================================================
::  OPTIE 5: Maak .env bestand aan
:: ============================================================
:CREATE_ENV
echo.
echo [INFO] .env bestand wordt aangemaakt...

if exist "%PROJECT_ROOT%\.env" (
    set /p OVERWRITE=".env bestaat al. Overschrijven? (j/N): "
    if /I not "!OVERWRITE!"=="j" (
        echo [INFO] .env is niet overschreven.
        pause
        goto MENU
    )
)

(
    echo # ClimateChecker Backend - lokale ontwikkelomgeving configuratie
    echo # Alle waarden hieronder gaan uit van lokale servers op localhost.
    echo.
    echo # --- Database MariaDB ---
    echo SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/ontdekstation013
    echo SPRING_DATASOURCE_USERNAME=root
    echo SPRING_DATASOURCE_PASSWORD=password
    echo SPRING_DATASOURCE_SCHEME=ontdekstation013
    echo.
    echo # --- Pad naar MariaDB bin-map (bijv. C:\Program Files\MariaDB\MariaDB 11.4\bin) ---
    echo # Laat dit leeg als mysql/mariadb al in je systeem-PATH staat.
    echo MARIADB_BIN_PATH=
    echo.
    echo # --- Mail server ---
    echo # false = gebruik in-memory GreenMail SMTP-server, true = gebruik echte SMTP relay
    echo USE_REAL_MAILSERVER=false
    echo MAILSERVER_RELAY_HOST=localhost
    echo MAILSERVER_RELAY_PORT=3025
    echo MAIL_USERNAME=test@localhost
    echo MAIL_PASSWORD=test
    echo.
    echo # --- Root gebruiker wordt aangemaakt bij eerste start ---
    echo ROOT_USER_FIRSTNAME=Admin
    echo ROOT_USER_LASTNAME=User
    echo ROOT_USER_EMAIL=admin@example.com
    echo ROOT_USER_PASSWORD=ChangeMe123!
    echo.
    echo # --- Frontend URL ---
    echo FRONTEND_HOST=http://localhost:5173
    echo.
    echo # --- Tooling versie voor mise ---
    echo NODE_VERSION=lts
) > "%PROJECT_ROOT%\.env"

echo [INFO] .env is aangemaakt in %PROJECT_ROOT%.env
echo [INFO] Pas de waarden aan indien nodig voor jouw omgeving.
pause
goto MENU

:: ============================================================
::  HULP ROUTINES
:: ============================================================

:LOAD_ENV
if not exist "%~1" exit /b 1
for /f "usebackq tokens=1* delims==" %%a in ("%~1") do (
    set "line=%%a"
    set "val=%%b"
    if defined line (
        set "firstchar=!line:~0,1!"
        if not "!firstchar!"=="#" (
            set "!line!=!val!"
        )
    )
)
exit /b 0

:PRINT_CONFIG
echo.
echo ============================================================
echo   Configuratie overzicht (uit .env en script)
echo ============================================================
echo [CONFIG] PROJECT_ROOT              = !PROJECT_ROOT!
echo [CONFIG] BACKEND_DIR               = !BACKEND_DIR!
echo [CONFIG] MISE_EXE                  = !MISE_EXE!
echo [CONFIG] BACKEND_PORT              = !BACKEND_PORT!
echo [CONFIG] SPRING_PROFILES_ACTIVE    = !SPRING_PROFILES_ACTIVE!
echo [CONFIG] SPRING_DATASOURCE_URL     = !SPRING_DATASOURCE_URL!
echo [CONFIG] SPRING_DATASOURCE_USERNAME= !SPRING_DATASOURCE_USERNAME!
echo [CONFIG] SPRING_DATASOURCE_SCHEME  = !SPRING_DATASOURCE_SCHEME!
echo [CONFIG] MARIADB_BIN_PATH          = !MARIADB_BIN_PATH!
echo [CONFIG] USE_REAL_MAILSERVER       = !USE_REAL_MAILSERVER!
echo [CONFIG] MAILSERVER_RELAY_HOST     = !MAILSERVER_RELAY_HOST!
echo [CONFIG] MAILSERVER_RELAY_PORT     = !MAILSERVER_RELAY_PORT!
echo [CONFIG] MAIL_USERNAME             = !MAIL_USERNAME!
echo [CONFIG] FRONTEND_HOST             = !FRONTEND_HOST!
echo [CONFIG] NODE_VERSION              = !NODE_VERSION!
echo ============================================================
echo.
exit /b 0

:ENSURE_DATABASE
if not defined SPRING_DATASOURCE_SCHEME (
    echo [WAARSCHUWING] SPRING_DATASOURCE_SCHEME is niet gedefinieerd. Sla database-controle over.
    exit /b 0
)

echo [INFO] Zoek mysql executable voor database-controle...
where mysql 2>nul
mysql --version >nul 2>&1

if !ERRORLEVEL! neq 0 (
    echo [WAARSCHUWING] mysql client niet gevonden. Kan database niet controleren/aanmaken.
    exit /b 0
)

echo [INFO] Controleer of database '%SPRING_DATASOURCE_SCHEME%' bestaat...
echo [INFO] Command: mysql -u "%SPRING_DATASOURCE_USERNAME%" -p"%SPRING_DATASOURCE_PASSWORD%" -e "CREATE DATABASE IF NOT EXISTS %SPRING_DATASOURCE_SCHEME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u "%SPRING_DATASOURCE_USERNAME%" -p"%SPRING_DATASOURCE_PASSWORD%" -e "CREATE DATABASE IF NOT EXISTS %SPRING_DATASOURCE_SCHEME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >nul 2>&1
if !ERRORLEVEL! neq 0 (
    echo [WAARSCHUWING] Kon database '%SPRING_DATASOURCE_SCHEME%' niet aanmaken. Mogelijk ontbreekt de database of zijn de inloggegevens incorrect.
) else (
    echo [INFO] Database '%SPRING_DATASOURCE_SCHEME%' is beschikbaar.
)
exit /b 0

:: ============================================================
::  AFSLUITEN
:: ============================================================
:EXIT_SCRIPT
echo.
echo [INFO] Script wordt afgesloten.
endlocal
exit /b 0
