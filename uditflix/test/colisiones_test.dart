import 'dart:ui';

import 'package:flutter_test/flutter_test.dart';
import 'package:uditflix/arcade/colisiones.dart';

void main() {
  test('detecta colisión cuando el personaje toca un obstáculo', () {
    final personaje = Rect.fromLTWH(40, 100, 38, 28);
    final obstaculo = Rect.fromLTWH(60, 90, 70, 200);

    expect(
      hayColision(personaje: personaje, obstaculo: obstaculo),
      isTrue,
    );
  });

  test('no detecta colisión cuando hay separación', () {
    final personaje = Rect.fromLTWH(40, 100, 38, 28);
    final obstaculo = Rect.fromLTWH(100, 0, 70, 200);

    expect(
      hayColision(personaje: personaje, obstaculo: obstaculo),
      isFalse,
    );
  });
}
