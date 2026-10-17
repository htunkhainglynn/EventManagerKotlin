import 'package:event_attendee/models.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('event JSON includes its session schedule', () {
    final event = EventInfo.fromJson({
      'id': 1,
      'name': 'Greenwich Mobile Conference',
      'date': '18/11/2026',
      'organiser': 'Computing Society',
      'location': 'Greenwich Campus',
      'description': 'Mobile development sessions',
      'contactEmail': 'events@example.org',
      'contactPhone': '+44 20 8331 8000',
      'sessions': [
        {
          'id': 2,
          'name': 'Opening Keynote',
          'time': '09:30',
          'speaker': 'Dr Maya Chen',
          'room': 'QA 180',
          'type': 'Keynote',
          'description': 'Opening talk',
        },
      ],
    });

    expect(event.name, 'Greenwich Mobile Conference');
    expect(event.sessions, hasLength(1));
    expect(event.sessions.single.type, 'Keynote');
  });
}
