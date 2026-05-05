# Hours Desktop Build (Windows MSI)

This project can be packaged as a Windows desktop installer (`.msi`) using `jpackage`.

## Prerequisites

- Windows 10/11
- **Пълен JDK 17+** (не само JRE): в инсталацията трябва да има папка `jmods` (напр. `%JAVA_HOME%\jmods`). Build-ът прави `jlink` с `ALL-MODULE-PATH` от там, за да няма „Failed to launch JVM“ с Spring Boot.
- `jpackage` в `PATH` (идва от JDK)
- Maven 3.9+
- WiX Toolset installed (required by `jpackage --type msi`)

## Database Runtime

The app now uses file-based H2 storage:

- JDBC URL: `jdbc:h2:file:${user.home}/.hours/data/hoursdb`
- Data persists between restarts in the user profile folder.
- Desktop packaging starts with `--spring.profiles.active=desktop`.
- При профил `desktop` след старт автоматично се отваря `http://localhost:<порт>/` в подразбирания браузър (виж `DesktopBrowserLauncher`).

## Build MSI

```powershell
.\mvnw.cmd -Pdesktop-windows clean package
```

`jpackage` за MSI изисква валиден Windows `ProductVersion` (напр. `1.2.3`). В `pom.xml` профилът ползва `desktop.app.version` (по подразбиране `0.0.6`), защото `0.0.1-SNAPSHOT` не е приемлив. При нужда:

```powershell
.\mvnw.cmd -Pdesktop-windows -Ddesktop.app.version=1.0.0 -DskipTests clean package
```

Output installer location:

- `target/installer/*.msi`

## Run and Verify

1. Install MSI.
2. Стартирай от менюто / работния плот: прякътът е в **Programs → TrackNG** (не в „Unknown“ — ползва се `--win-menu-group`).
3. Open `http://localhost:8086`.
4. Login with bootstrap admin (or configured env overrides).

## H2 Persistence Smoke Test

1. Create a test worker and one work entry.
2. Close the application.
3. Start it again.
4. Confirm the worker and entry still exist.

## Ако Hours.exe показва „Failed to launch JVM“

1. Намери инсталационната папка (често `C:\Program Files\Hours` или пътя, който си избрал при инсталация).
2. В PowerShell (смени пътя при нужда):

```powershell
$base = "C:\Program Files\Hours"
Get-ChildItem $base -Recurse -Depth 2 | Select-Object FullName
& "$base\runtime\bin\java.exe" -version
Get-ChildItem "$base\app"
& "$base\runtime\bin\java.exe" -jar "$base\app\hours-0.0.1-SNAPSHOT.jar"
```

- Ако `java -version` **не** тръгва → проблем с вградения runtime (инсталация/антивирус).
- Ако `java -jar ...` **тръгва**, а `Hours.exe` не → проблем в нативния launcher на `jpackage` (пиши резултата от командите).
- Ако `java -jar` показва **Spring грешка** (порт, DB и т.н.) → това вече не е „JVM“, а приложението; грешката ще е в конзолата. Пример: H2 `Feature not supported: "AUTO_SERVER=TRUE && DB_CLOSE_ON_EXIT=FALSE"` — вече е оправено в `application.properties` (без тази комбинация в URL).

След промени в конфигурацията направи нов MSI: `.\mvnw.cmd -Pdesktop-windows -DskipTests clean package` и инсталирай **`Hours-0.0.6.msi`** (или текущата `desktop.app.version` от `pom.xml`).

## Optional Environment Overrides

- `SERVER_PORT`
- `APP_JWT_SECRET`
- `APP_JWT_EXPIRATION_MS`
- `APP_BOOTSTRAP_ADMIN_PHONE`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`
- `APP_BOOTSTRAP_ADMIN_NAME`
