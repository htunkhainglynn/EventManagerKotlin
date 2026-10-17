import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

import 'models.dart';

class EventApiException implements Exception {
  const EventApiException(this.message);
  final String message;
  @override
  String toString() => message;
}

String defaultEventApiUrl() {
  const configured = String.fromEnvironment('EVENT_API_URL');
  if (configured.isNotEmpty) return configured;
  if (kIsWeb || defaultTargetPlatform == TargetPlatform.iOS) {
    return 'http://localhost:8080/api/events';
  }
  return 'http://10.0.2.2:8080/api/events';
}

class EventApi {
  EventApi({String? baseUrl, http.Client? client})
      : baseUrl = baseUrl ?? defaultEventApiUrl(),
        _client = client ?? http.Client();

  final String baseUrl;
  final http.Client _client;

  Future<List<EventInfo>> fetchEvents(
      {String name = '', DateTime? date}) async {
    final base = Uri.parse(baseUrl);
    final query = <String, String>{
      if (name.trim().isNotEmpty) 'name': name.trim(),
      if (date != null) 'date': _isoDate(date),
    };
    final uri = base.replace(queryParameters: query.isEmpty ? null : query);

    late http.Response response;
    try {
      response = await _client.get(uri).timeout(const Duration(seconds: 15));
    } catch (_) {
      throw const EventApiException(
        'Could not reach the event service. Check the API address and network.',
      );
    }
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw EventApiException(
          'Event service returned HTTP ${response.statusCode}.');
    }

    try {
      final payload = jsonDecode(response.body) as Map<String, dynamic>;
      return (payload['events'] as List<dynamic>? ?? const [])
          .map((value) => EventInfo.fromJson(value as Map<String, dynamic>))
          .toList(growable: false);
    } catch (_) {
      throw const EventApiException('The event service returned invalid data.');
    }
  }

  void close() => _client.close();
}

String _isoDate(DateTime date) =>
    '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
