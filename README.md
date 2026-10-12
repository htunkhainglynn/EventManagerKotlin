# Event Manager Kotlin

Native Android implementation of the COMP1786 CW2 Event Manager admin app. It is written in Kotlin with Jetpack Compose and stores data locally with Room (SQLite). This project also includes a separate Java BMI Calculator app for Exercise 1.

## Implemented features

- Create, review, edit, list, and delete events.
- Required-field validation plus strict `DD/MM/YYYY`, email, and phone validation.
- Duplicate event prevention using the combination of event name and date.
- Create, edit, search, and delete multiple sessions for each event.
- Automatic session IDs and the required session type choices.
- Event-to-session relationship with cascade deletion and a session count.
- Event search by name or description and advanced filtering by date or organiser.
- Session search by name or description.
- Network-aware background upload of all events and sessions as JSON.
- Shareable plain-text event schedule as an additional feature.
- Light and dark themes with a responsive Compose interface.

## Open and run

1. Open this `EventManagerKotlin` folder in Android Studio.
2. Let Android Studio install Android SDK 35 and sync the Gradle project.
3. Select an emulator or an Android device running Android 8.0 (API 26) or newer.
4. Run the `app` configuration for Event Manager, or `bmiapp` for the Exercise 1 BMI Calculator.

From a terminal with `ANDROID_HOME` configured, use:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The Event Manager debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. The BMI Calculator APK is generated at `bmiapp/build/outputs/apk/debug/bmiapp-debug.apk`.

## Exercise 1 BMI Calculator

The `bmiapp` module installs as a separate app. It accepts kilograms or pounds for weight and centimetres or metres for height, validates both values against reasonable ranges, calculates BMI, and shows the adult BMI category in both words and colour. The Java calculation class is unit-tested separately from the screen. On a borrowed computer, select the `bmiapp` run configuration, then capture screenshots of valid results in each unit and invalid-input messages for the Exercise 1 logbook.

## Cloud endpoint

The project uses `https://httpbin.org/post` as a safe demonstration endpoint, so the upload path can be run immediately and its HTTP behavior can be demonstrated. It accepts the JSON but does not persist it.

For the final coursework cloud database, replace `UPLOAD_ENDPOINT` in `app/build.gradle.kts` with the POST endpoint supplied by your own service. The complete payload contains an `events` array, and each event contains its nested `sessions` array. Successful uploads record a local sync timestamp; any later event or session change marks the event as waiting to upload again.

## Project structure

- `data/` contains Room entities, DAOs, the database, and repository.
- `model/Validation.kt` contains reusable validation rules.
- `sync/CloudUploadWorker.kt` performs the background upload.
- `ui/` contains the Compose screens, navigation, and view model.
- `app/src/test/` contains local validation tests.

No report or PDF is included in this project.
