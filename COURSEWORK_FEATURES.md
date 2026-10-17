# Coursework feature checklist

This checklist maps the COMP1786 coursework requirements to runnable code in this repository.

| Ref | Requirement | Implementation |
| --- | --- | --- |
| A | Add, validate, confirm, edit and delete events | The Kotlin admin app provides Compose forms and confirmation screens. Required fields, `DD/MM/YYYY`, email, phone and duplicate name/date rules are validated before Room storage. |
| B | Add, edit and delete multiple sessions | Each event can own any number of sessions. Room creates automatic IDs; the form validates session name/time and provides the required type choices plus optional speaker, room and description. |
| C | Link events and sessions | `SessionEntity.eventId` links the tables, the event list shows each session count, detail screens show the schedule, and the foreign key cascades session deletion when an event is removed. |
| D | Search | Admin users can partially search events by name/description, filter by exact date and organiser, and partially search sessions by name/description. |
| E | Cloud upload | WorkManager checks for a validated network and sends the complete event/session JSON to the persistent service. Every event or session write queues a new sync, including deletion through full replacement. Failed network/server attempts are retried. |
| F | Extra feature | The admin app creates a shareable plain-text schedule for an event, with its session information. |
| G | Hybrid attendee app | The Flutter app runs on Android, iOS and web, reads the shared service, lists event/session details, refreshes data, and searches by event name and exact date. |
| H | Favourites | Attendees can add/remove favourites from either list or detail screens and filter the list to favourites. IDs persist locally with `shared_preferences`. |

## Components

- `app/` — Kotlin/Jetpack Compose Android administration app.
- `cloud-service/` — persistent Node.js JSON API shared by both apps.
- `attendee_app/` — Flutter hybrid attendee app for Android, iOS and web.
- `bmiapp/` — separate Java BMI Calculator from Exercise 1.

The cloud service is deployment-ready through its `Dockerfile`. A deployment must mount durable storage at `/app/data`, set a secret `ADMIN_API_KEY`, and expose HTTPS. Configure the same endpoint/key in the native admin build and the endpoint in the Flutter build.
