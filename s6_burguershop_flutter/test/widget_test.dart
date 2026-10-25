import 'package:flutter_test/flutter_test.dart';

import 'package:s6_burguershop_flutter/main.dart';

void main() {
  testWidgets('BurguerShop app smoke test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const BurguerShopApp());

    // Verify that our app title/catalog is displayed.
    expect(find.text('🍔 BurguerShop'), findsOneWidget);
  });
}
