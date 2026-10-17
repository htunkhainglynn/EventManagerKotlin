class EventSession {
  const EventSession({
    required this.id,
    required this.name,
    required this.time,
    required this.speaker,
    required this.room,
    required this.type,
    required this.description,
  });

  final int id;
  final String name;
  final String time;
  final String speaker;
  final String room;
  final String type;
  final String description;

  factory EventSession.fromJson(Map<String, dynamic> json) => EventSession(
        id: (json['id'] as num).toInt(),
        name: json['name'] as String? ?? '',
        time: json['time'] as String? ?? '',
        speaker: json['speaker'] as String? ?? '',
        room: json['room'] as String? ?? '',
        type: json['type'] as String? ?? 'Others',
        description: json['description'] as String? ?? '',
      );
}

class EventInfo {
  const EventInfo({
    required this.id,
    required this.name,
    required this.date,
    required this.organiser,
    required this.location,
    required this.description,
    required this.contactEmail,
    required this.contactPhone,
    required this.sessions,
  });

  final int id;
  final String name;
  final String date;
  final String organiser;
  final String location;
  final String description;
  final String contactEmail;
  final String contactPhone;
  final List<EventSession> sessions;

  factory EventInfo.fromJson(Map<String, dynamic> json) => EventInfo(
        id: (json['id'] as num).toInt(),
        name: json['name'] as String? ?? '',
        date: json['date'] as String? ?? '',
        organiser: json['organiser'] as String? ?? '',
        location: json['location'] as String? ?? '',
        description: json['description'] as String? ?? '',
        contactEmail: json['contactEmail'] as String? ?? '',
        contactPhone: json['contactPhone'] as String? ?? '',
        sessions: (json['sessions'] as List<dynamic>? ?? const [])
            .map(
                (value) => EventSession.fromJson(value as Map<String, dynamic>))
            .toList(growable: false),
      );
}
