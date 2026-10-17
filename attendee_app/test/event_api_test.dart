import 'package:event_attendee/event_api.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  test('API sends name/date search and parses events', () async {
    final client = MockClient((request) async {
      expect(request.url.queryParameters['name'], 'Greenwich');
      expect(request.url.queryParameters['date'], '18/11/2026');
      return http.Response(
        '''{"events":[{"id":1,"name":"Greenwich Mobile Conference","date":"18/11/2026","organiser":"Computing Society","location":"Greenwich Campus","description":"","contactEmail":"","contactPhone":"","sessions":[]}]}''',
        200,
      );
    });
    final api =
        EventApi(baseUrl: 'https://events.example/api/events', client: client);

    final events = await api.fetchEvents(
      name: ' Greenwich ',
      date: '18/11/2026',
    );

    expect(events, hasLength(1));
    expect(events.single.name, 'Greenwich Mobile Conference');
    api.close();
  });

  test('API reports a server error clearly', () async {
    final api = EventApi(
      baseUrl: 'https://events.example/api/events',
      client: MockClient((_) async => http.Response('{}', 503)),
    );

    expect(
      api.fetchEvents(),
      throwsA(
        isA<EventApiException>().having(
          (error) => error.message,
          'message',
          contains('503'),
        ),
      ),
    );
    api.close();
  });
}
