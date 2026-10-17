import 'package:flutter/material.dart';

import 'event_api.dart';
import 'favourites.dart';
import 'models.dart';

void main() => runApp(const EventAttendeeApp());

class EventAttendeeApp extends StatelessWidget {
  const EventAttendeeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Event Explorer',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xff295ea8)),
        useMaterial3: true,
        inputDecorationTheme:
            const InputDecorationTheme(border: OutlineInputBorder()),
      ),
      home: const EventBrowserPage(),
    );
  }
}

class EventBrowserPage extends StatefulWidget {
  const EventBrowserPage({super.key, this.api});
  final EventApi? api;

  @override
  State<EventBrowserPage> createState() => _EventBrowserPageState();
}

class _EventBrowserPageState extends State<EventBrowserPage> {
  late final EventApi _api = widget.api ?? EventApi();
  final _nameController = TextEditingController();
  final _dateController = TextEditingController();
  List<EventInfo> _events = const [];
  Set<int> _favourites = const {};
  FavouritesStore? _favouritesStore;
  bool _loading = true;
  bool _favouritesOnly = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _initialise();
  }

  Future<void> _initialise() async {
    final store = await FavouritesStore.open();
    if (!mounted) return;
    setState(() {
      _favouritesStore = store;
      _favourites = store.read();
    });
    await _loadEvents();
  }

  Future<void> _loadEvents() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final events = await _api.fetchEvents(
        name: _nameController.text,
        date: _dateController.text,
      );
      if (mounted) setState(() => _events = events);
    } on EventApiException catch (error) {
      if (mounted) setState(() => _error = error.message);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _toggleFavourite(int eventId) async {
    final store = _favouritesStore;
    if (store == null) return;
    final values = await store.toggle(eventId);
    if (mounted) setState(() => _favourites = values);
  }

  @override
  void dispose() {
    _api.close();
    _nameController.dispose();
    _dateController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final visibleEvents = _favouritesOnly
        ? _events.where((event) => _favourites.contains(event.id)).toList()
        : _events;
    return Scaffold(
      appBar: AppBar(
        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Event Explorer'),
            Text('Find your next session', style: TextStyle(fontSize: 12)),
          ],
        ),
        actions: [
          IconButton(
            tooltip: 'Refresh events',
            onPressed: _loading ? null : _loadEvents,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: _loadEvents,
          child: ListView(
            padding: const EdgeInsets.all(16),
            physics: const AlwaysScrollableScrollPhysics(),
            children: [
              _SearchPanel(
                nameController: _nameController,
                dateController: _dateController,
                loading: _loading,
                onSearch: _loadEvents,
                onClear: () {
                  _nameController.clear();
                  _dateController.clear();
                  _loadEvents();
                },
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  FilterChip(
                    selected: _favouritesOnly,
                    avatar: const Icon(Icons.star, size: 18),
                    label: Text('Favourites (${_favourites.length})'),
                    onSelected: (value) =>
                        setState(() => _favouritesOnly = value),
                  ),
                  const Spacer(),
                  if (!_loading && _error == null)
                    Text(
                        '${visibleEvents.length} event${visibleEvents.length == 1 ? '' : 's'}'),
                ],
              ),
              const SizedBox(height: 12),
              if (_loading)
                const Padding(
                  padding: EdgeInsets.all(32),
                  child: Center(child: CircularProgressIndicator()),
                )
              else if (_error != null)
                _MessageState(
                  icon: Icons.cloud_off,
                  title: 'Events unavailable',
                  message: _error!,
                  action: FilledButton.icon(
                    onPressed: _loadEvents,
                    icon: const Icon(Icons.refresh),
                    label: const Text('Try again'),
                  ),
                )
              else if (visibleEvents.isEmpty)
                _MessageState(
                  icon: _favouritesOnly ? Icons.star_border : Icons.event_busy,
                  title: _favouritesOnly
                      ? 'No favourites yet'
                      : 'No matching events',
                  message: _favouritesOnly
                      ? 'Tap the star on an event to keep it here.'
                      : 'Try another name or date.',
                )
              else
                ...visibleEvents.map(
                  (event) => Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: _EventCard(
                      event: event,
                      favourite: _favourites.contains(event.id),
                      onFavourite: () => _toggleFavourite(event.id),
                      onOpen: () => Navigator.of(context).push(
                        MaterialPageRoute<void>(
                          builder: (_) => EventDetailPage(
                            event: event,
                            favourite: _favourites.contains(event.id),
                            onFavourite: () => _toggleFavourite(event.id),
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SearchPanel extends StatelessWidget {
  const _SearchPanel({
    required this.nameController,
    required this.dateController,
    required this.loading,
    required this.onSearch,
    required this.onClear,
  });

  final TextEditingController nameController;
  final TextEditingController dateController;
  final bool loading;
  final VoidCallback onSearch;
  final VoidCallback onClear;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('Search available events',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            TextField(
              controller: nameController,
              textInputAction: TextInputAction.next,
              decoration: const InputDecoration(
                labelText: 'Event name',
                prefixIcon: Icon(Icons.search),
              ),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: dateController,
              keyboardType: TextInputType.datetime,
              onSubmitted: (_) => onSearch(),
              decoration: const InputDecoration(
                labelText: 'Date',
                hintText: 'DD/MM/YYYY',
                prefixIcon: Icon(Icons.calendar_month),
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: FilledButton.icon(
                    onPressed: loading ? null : onSearch,
                    icon: const Icon(Icons.search),
                    label: const Text('Search'),
                  ),
                ),
                const SizedBox(width: 8),
                TextButton(
                    onPressed: loading ? null : onClear,
                    child: const Text('Clear')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _EventCard extends StatelessWidget {
  const _EventCard({
    required this.event,
    required this.favourite,
    required this.onFavourite,
    required this.onOpen,
  });

  final EventInfo event;
  final bool favourite;
  final VoidCallback onFavourite;
  final VoidCallback onOpen;

  @override
  Widget build(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onOpen,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text(event.name,
                        style: Theme.of(context).textTheme.titleLarge),
                  ),
                  IconButton(
                    tooltip: favourite
                        ? 'Remove from favourites'
                        : 'Add to favourites',
                    onPressed: onFavourite,
                    icon: Icon(favourite ? Icons.star : Icons.star_border),
                  ),
                ],
              ),
              const Divider(),
              _Fact(icon: Icons.calendar_month, text: event.date),
              _Fact(icon: Icons.location_on, text: event.location),
              _Fact(icon: Icons.groups, text: event.organiser),
              const SizedBox(height: 8),
              Text(
                  '${event.sessions.length} session${event.sessions.length == 1 ? '' : 's'}'),
            ],
          ),
        ),
      ),
    );
  }
}

class EventDetailPage extends StatefulWidget {
  const EventDetailPage({
    super.key,
    required this.event,
    required this.favourite,
    required this.onFavourite,
  });

  final EventInfo event;
  final bool favourite;
  final Future<void> Function() onFavourite;

  @override
  State<EventDetailPage> createState() => _EventDetailPageState();
}

class _EventDetailPageState extends State<EventDetailPage> {
  late bool _favourite = widget.favourite;

  Future<void> _toggle() async {
    await widget.onFavourite();
    if (mounted) setState(() => _favourite = !_favourite);
  }

  @override
  Widget build(BuildContext context) {
    final event = widget.event;
    return Scaffold(
      appBar: AppBar(
        title: Text(event.name),
        actions: [
          IconButton(
            tooltip:
                _favourite ? 'Remove from favourites' : 'Add to favourites',
            onPressed: _toggle,
            icon: Icon(_favourite ? Icons.star : Icons.star_border),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            color: Theme.of(context).colorScheme.primaryContainer,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(event.name,
                      style: Theme.of(context).textTheme.headlineSmall),
                  const SizedBox(height: 12),
                  _Fact(icon: Icons.calendar_month, text: event.date),
                  _Fact(icon: Icons.location_on, text: event.location),
                  _Fact(
                      icon: Icons.groups,
                      text: 'Organised by ${event.organiser}'),
                  if (event.description.isNotEmpty) ...[
                    const Divider(),
                    Text(event.description),
                  ],
                  if (event.contactEmail.isNotEmpty)
                    _Fact(icon: Icons.email, text: event.contactEmail),
                  if (event.contactPhone.isNotEmpty)
                    _Fact(icon: Icons.phone, text: event.contactPhone),
                ],
              ),
            ),
          ),
          const SizedBox(height: 20),
          Text('Schedule', style: Theme.of(context).textTheme.headlineSmall),
          const SizedBox(height: 8),
          if (event.sessions.isEmpty)
            const Text('No sessions have been published for this event.')
          else
            ...event.sessions.map(
              (session) => Card(
                child: ListTile(
                  leading: Container(
                    width: 52,
                    alignment: Alignment.center,
                    decoration: BoxDecoration(
                      color: Theme.of(context).colorScheme.secondaryContainer,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      session.time,
                      maxLines: 1,
                      style: Theme.of(context).textTheme.labelMedium,
                    ),
                  ),
                  title: Text(session.name),
                  subtitle: Text([
                    session.type,
                    if (session.speaker.isNotEmpty) session.speaker,
                    if (session.room.isNotEmpty) 'Room ${session.room}',
                    if (session.description.isNotEmpty) session.description,
                  ].join(' • ')),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _Fact extends StatelessWidget {
  const _Fact({required this.icon, required this.text});
  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Row(
          children: [
            Icon(icon, size: 20, color: Theme.of(context).colorScheme.primary),
            const SizedBox(width: 8),
            Expanded(child: Text(text)),
          ],
        ),
      );
}

class _MessageState extends StatelessWidget {
  const _MessageState({
    required this.icon,
    required this.title,
    required this.message,
    this.action,
  });

  final IconData icon;
  final String title;
  final String message;
  final Widget? action;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 48, horizontal: 24),
        child: Column(
          children: [
            Icon(icon, size: 52, color: Theme.of(context).colorScheme.primary),
            const SizedBox(height: 12),
            Text(title, style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 6),
            Text(message, textAlign: TextAlign.center),
            if (action != null) ...[const SizedBox(height: 16), action!],
          ],
        ),
      );
}
