class EventSession {
  const EventSession({
    required this.id,
    required this.name,
    required this.startMinutes,
    required this.speaker,
    required this.room,
    required this.type,
    required this.description,
  });

  final int id;
  final String name;
  final int startMinutes;
  final String speaker;
  final String room;
  final String type;
  final String description;

  factory EventSession.fromJson(Map<String, dynamic> json) => EventSession(
        id: (json['id'] as num).toInt(),
        name: json['name'] as String? ?? '',
        startMinutes: (json['startMinutes'] as num).toInt(),
        speaker: json['speaker'] as String? ?? '',
        room: json['room'] as String? ?? '',
        type: json['type'] as String? ?? 'Others',
        description: json['description'] as String? ?? '',
      );

  String get formattedTime {
    final hour = startMinutes ~/ 60;
    final minute = startMinutes % 60;
    return '${hour.toString().padLeft(2, '0')}:${minute.toString().padLeft(2, '0')}';
  }
}

class EventInfo {
  const EventInfo({
    required this.id,
    required this.name,
    required this.date,
    required this.organiser,
    required this.location,
    required this.eventType,
    required this.status,
    required this.description,
    required this.contactEmail,
    required this.contactPhone,
    required this.sessions,
  });

  final int id;
  final String name;
  final DateTime date;
  final String organiser;
  final String location;
  final String eventType;
  final String status;
  final String description;
  final String contactEmail;
  final String contactPhone;
  final List<EventSession> sessions;

  factory EventInfo.fromJson(Map<String, dynamic> json) => EventInfo(
        id: (json['id'] as num).toInt(),
        name: json['name'] as String? ?? '',
        date: DateTime.parse(json['date'] as String),
        organiser: json['organiser'] as String? ?? '',
        location: json['location'] as String? ?? '',
        eventType: json['eventType'] as String? ?? 'Other',
        status: json['status'] as String? ?? 'Upcoming',
        description: json['description'] as String? ?? '',
        contactEmail: json['contactEmail'] as String? ?? '',
        contactPhone: json['contactPhone'] as String? ?? '',
        sessions: (json['sessions'] as List<dynamic>? ?? const [])
            .map(
                (value) => EventSession.fromJson(value as Map<String, dynamic>))
            .toList(growable: false),
      );

  String get formattedDate =>
      '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year.toString().padLeft(4, '0')}';
}
