import 'package:shared_preferences/shared_preferences.dart';

class FavouritesStore {
  FavouritesStore(this._preferences);

  static const _key = 'favourite_event_ids';
  final SharedPreferences _preferences;

  static Future<FavouritesStore> open() async =>
      FavouritesStore(await SharedPreferences.getInstance());

  Set<int> read() => (_preferences.getStringList(_key) ?? const <String>[])
      .map(int.tryParse)
      .whereType<int>()
      .toSet();

  Future<Set<int>> toggle(int eventId) async {
    final values = read();
    if (!values.add(eventId)) values.remove(eventId);
    final encoded = values.map((value) => value.toString()).toList()..sort();
    await _preferences.setStringList(_key, encoded);
    return values;
  }
}
