# Event Attendee Flutter App

This is the hybrid user app for coursework features G and H. It reads events and nested sessions from the shared Event Manager API, searches by event name and a date picker, and stores favourite event IDs on the device.

The service exposes dates as ISO `YYYY-MM-DD` values and session times as integer minutes after midnight. The Flutter model parses those values into appropriate date/time representations instead of using free-form user input.

## Run

Start the service first:

```text
cd ../cloud-service
npm start
```

Then run Flutter:

```text
flutter pub get
flutter run
```

The Android emulator uses `http://10.0.2.2:8080/api/events`. Flutter web and the iOS simulator use `http://localhost:8080/api/events`.

For a deployed HTTPS service:

```text
flutter run --dart-define=EVENT_API_URL=https://your-service.example/api/events
```

## Verify

```text
flutter analyze
flutter test --platform chrome
flutter build web
```
