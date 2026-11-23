import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:uditflix/main.dart';
import 'package:uditflix/screens/ficha_screen.dart';

void main() {
  testWidgets('el catálogo muestra UDITflix, destacados y el acceso a Arcade', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const MyApp());

    expect(find.text('UDITflix'), findsOneWidget);
    expect(find.text('Destacados'), findsOneWidget);
    expect(find.text('Introduccion a flutter'), findsWidgets);
    expect(find.byTooltip('UDIT Arcade'), findsOneWidget);

    final scrollable = find.byType(Scrollable).first;
    for (final category in ['Vídeos', 'Imágenes', 'Videojuegos']) {
      await tester.scrollUntilVisible(
        find.text(category),
        250,
        scrollable: scrollable,
      );
      expect(find.text(category), findsOneWidget);
    }
  });

  testWidgets('tocar un contenido abre su ficha con el botón de reproducción', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const MyApp());

    final comic = find.text('El mundo de los comics');
    await tester.scrollUntilVisible(
      comic,
      250,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.tap(comic);
    await tester.pump();
    await tester.pump(const Duration(seconds: 1));

    expect(find.byType(FichaScreen), findsOneWidget);
    expect(find.text('Reproducir vídeo'), findsOneWidget);
  });

  testWidgets('el botón de UDIT Arcade abre la pantalla del juego', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const MyApp());

    await tester.tap(find.byTooltip('UDIT Arcade'));
    // No usamos pumpAndSettle: Flame mantiene un ticker activo.
    await tester.pump(const Duration(milliseconds: 300));
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text('Toca para saltar'), findsOneWidget);
    expect(find.text('Puntos: 0'), findsOneWidget);
  });

  testWidgets('el botón de reiniciar vuelve a iniciar la partida', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const MyApp());
    await tester.tap(find.byTooltip('UDIT Arcade'));
    await tester.pump();

    for (var frame = 0; frame < 20; frame++) {
      await tester.pump(const Duration(milliseconds: 50));
    }

    expect(find.text('GAME OVER'), findsOneWidget);
    await tester.tap(find.text('Reiniciar partida'));
    await tester.pump();

    expect(find.text('GAME OVER'), findsNothing);
    expect(find.text('Puntos: 0'), findsOneWidget);
  });
}
