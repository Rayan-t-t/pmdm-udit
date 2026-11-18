import 'dart:ui';

// Comprobación sencilla de colisión rectángulo contra rectángulo.
// Se usa en el juego para detectar que el personaje toca un obstáculo.
bool hayColision({required Rect personaje, required Rect obstaculo}) {
  return personaje.overlaps(obstaculo);
}
