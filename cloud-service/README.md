# Event Manager Cloud Service

This small persistent JSON API is shared by the Android admin app and the Flutter attendee app. It uses only Node.js built-in modules, so no package installation is required.

## Run locally

```text
cd cloud-service
npm test
npm start
```

The service listens on port `8080`, stores data in `data/events.json`, and exposes:

- `GET /health`
- `GET /api/events?name=&date=` for the attendee app; dates use ISO `YYYY-MM-DD`
- `PUT /api/events` for a complete admin synchronisation

Set `ADMIN_API_KEY` to protect the write endpoint. The admin app sends the matching value from the Gradle property `EVENT_API_KEY`.

## Configure the apps

The Android emulator reaches the host service at `http://10.0.2.2:8080/api/events`, which is the native app default. Use a deployed HTTPS URL on a physical device; local cleartext access is deliberately limited to the emulator and localhost.

The Flutter app defaults to `http://localhost:8080/api/events` on web and `http://10.0.2.2:8080/api/events` on Android. Override it with:

```text
flutter run --dart-define=EVENT_API_URL=https://your-service.example/api/events
```

For a hosted deployment, keep the persistent `DATA_FILE` on durable storage, set `ADMIN_API_KEY`, and use HTTPS. Then build both apps with the hosted endpoint.

## Docker deployment

The included `Dockerfile` starts the service on port `8080`. Mount durable storage at `/app/data` so uploaded records survive restarts:

```text
docker build -t event-manager-api .
docker run --rm -p 8080:8080 -v event-manager-data:/app/data -e ADMIN_API_KEY=change-me event-manager-api
```
