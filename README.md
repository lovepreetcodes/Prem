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


## v1.2.1 UI and interaction patch (2026-11-01)

- Dashboard stat cards now use a two-column layout and the goal card spans the full width for more consistent sizing.
- Goal setup now asks for a separate starting weight and target weight. The starting value is stored independently from daily weigh-ins.
- Weight chart now supports point selection, selected-value details, a shaded trend area, and a target reference line when the target is within the plotted range.
- Workout screen can be opened by swiping left from the weight screen and returned to by swiping right. The workout screen shows separate Upper and Lower session trend cards.
- Existing Room migration and Health Connect integration are retained.

Build validation: this source patch has not yet been compiled or installed on a device. Run the repository GitHub Actions workflow and test the resulting APK on the phone before treating it as a verified release.
