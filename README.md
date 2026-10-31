# Prem Android — v1.0

A native Kotlin + Jetpack Compose weight and activity tracker. Study and diary features are intentionally out of scope for this first release.

## Features
- Dark dashboard with monthly calendar and weight-entry markers.
- Manual daily weight logging, editing and deletion.
- Weight history stored locally with Room.
- Weight trend chart (last 30 entries).
- Health Connect step reading for the selected calendar date using aggregated `StepsRecord.COUNT_TOTAL` to avoid double-counting sources.
- Explicit Health Connect permission flow and manual refresh.
- GitHub Actions debug APK build.

## Samsung Health setup
1. Update Samsung Health on the Galaxy S20 FE.
2. Install/update Health Connect if the phone's Android version requires it.
3. In Samsung Health settings, open Health Connect and allow Samsung Health to share Steps.
4. In Prem, tap Connect and grant read access to Steps.
5. Return to Prem and tap Refresh when needed.

Prem reads step totals from Health Connect. It does not connect to Samsung Health's private database or use Samsung's retired legacy SDK. Availability depends on the phone's Android/Health Connect version and Samsung Health sync settings.

## Build
Open this folder as a Gradle project in Android Studio (JDK 17, Android SDK 36), or push to a GitHub repository with the default branch named `main`. GitHub Actions builds `app-debug.apk` and uploads it as `Prem-debug-apk`.

## Data and limitations
- Weight records are stored locally in the app database. Clearing app data or uninstalling can delete them; export/backup is a planned next feature and should be added before relying on Prem as the only record.
- Steps are read on app open and on manual refresh in this first version. This is not a continuous background sync.
- Health Connect may require install/update depending on Android version. On Android 14+ it is part of the system; Android 13 and lower need the Health Connect app.
- This is a starter v1.0 implementation; build and test on the target S20 FE before daily use.


## Android 13 (Galaxy S20 FE)
On Android 13, install/update the Health Connect app from Google Play. In Samsung Health, open Settings → Health Connect and enable sharing for Steps. Then open Prem → Connect and grant read access. Health Connect's Android 13 provider is separate from the Android 14+ system module.

## Build configuration
This project uses Android Gradle Plugin 8.9.1, Gradle 8.11.1, Kotlin 2.0.21, Compose compiler plugin 2.0.21, compileSdk 36 and the stable Health Connect client 1.1.0.
