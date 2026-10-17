import 'package:event_attendee/favourites.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  test('favourites persist when toggled', () async {
    SharedPreferences.setMockInitialValues({});
    final store = await FavouritesStore.open();

    expect(store.read(), isEmpty);
    expect(await store.toggle(42), {42});
    expect(store.read(), {42});
    expect(await store.toggle(42), isEmpty);
  });
}
