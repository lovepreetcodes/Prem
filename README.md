# Prem Android v1.2

Native Kotlin + Jetpack Compose fitness tracker. This release builds on v1.1 and preserves the existing Room database and Health Connect integration.

## v1.2 changes
- Updated adaptive launcher icon and density-specific legacy launcher assets.
- Room v1-to-v2 migration that preserves existing weight entries and adds workout sessions, workout sets, and goal-weight storage.
- Upper/lower workout templates with the configured 13 exercises and three sets per exercise.
- Manual weight/repetition entry, saved workout history, previous-set reference, and per-exercise weight/repetition trend charts.
- Goal-weight card with circular progress and remaining kilograms.
- Green weight trend graph with 7/30/90-day ranges and a 7-entry average comparison against the previous 7 entries.
- Existing Health Connect step reading retained.

## Build
Open this `Prem_Kotlin` directory in Android Studio or push it to the configured GitHub repository. GitHub Actions builds a debug APK with `gradle assembleDebug`.

The project requires JDK 17, Android SDK 36, and Gradle 8.11.1.

## Notes
- Workout records and weight records are stored locally in Room.
- The 7-day trend comparison uses the latest seven logged weight entries versus the preceding seven logged entries; it is an entry-based comparison, not a requirement for one entry on every calendar day.
- The generated ZIP contains source code; the APK must be built by Android Studio or GitHub Actions.
