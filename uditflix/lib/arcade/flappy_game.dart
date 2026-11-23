import 'dart:math';

import 'package:flame/components.dart';
import 'package:flame/events.dart';
import 'package:flame/game.dart';
import 'package:flutter/material.dart';

import 'colisiones.dart';

// Parámetros sencillos del juego, pensados para una demo educativa.
const double _pipeWidth = 70;
const double _gapHeight = 190;
const double _pipeSpeed = 170;
const double _spawnInterval = 1.6;
const double _groundHeight = 70;
const double _gravity = 1500;
const double _flapSpeed = -480;
const double _birdWidth = 38;
const double _birdHeight = 28;

// Juego type Flappy Bird implementado con Flame.
// El bucle de update se encarga de la gravedad, los obstáculos,
// la puntuación y las colisiones.
class UditFlappyGame extends FlameGame with TapCallbacks {
  UditFlappyGame({
    required this.onScoreChanged,
    required this.onGameOver,
  });

  final ValueChanged<int> onScoreChanged;
  final VoidCallback onGameOver;

  final Random _random = Random();
  final List<_Pipe> _pipes = [];

  late final RectangleComponent _bird;
  double _verticalSpeed = 0;
  double _spawnTimer = 0;
  int _score = 0;
  bool _isGameOver = false;

  @override
  Future<void> onLoad() async {
    await super.onLoad();

    _bird = RectangleComponent(
      position: Vector2(size.x * 0.22, size.y * 0.4),
      size: Vector2(_birdWidth, _birdHeight),
      paint: Paint()..color = const Color(0xFFFFC857),
      priority: 10,
    );
    add(_bird);

    add(RectangleComponent(
      position: Vector2(0, size.y - _groundHeight),
      size: Vector2(size.x, _groundHeight),
      paint: Paint()..color = const Color(0xFF3E2A63),
    ));

    // Primera tubería casi enseguida para que la demo sea dinámica.
    _spawnTimer = _spawnInterval;
  }

  // Cada pulsación hace "saltar" al personaje.
  @override
  void onTapDown(TapDownEvent event) {
    if (_isGameOver) return;
    _verticalSpeed = _flapSpeed;
  }

  @override
  void update(double dt) {
    super.update(dt);
    if (_isGameOver || dt <= 0) return;

    // Limitamos el paso para evitar saltos grandes tras cambiar de pestaña.
    final step = dt > 0.05 ? 0.05 : dt;

    _verticalSpeed += _gravity * step;
    _bird.position.y += _verticalSpeed * step;

    if (_bird.position.y < 0) {
      _bird.position.y = 0;
      _verticalSpeed = 0;
    }

    final groundY = size.y - _groundHeight;
    if (_bird.position.y + _birdHeight >= groundY) {
      _endGame();
      return;
    }

    _spawnTimer += step;
    if (_spawnTimer >= _spawnInterval) {
      _spawnTimer = 0;
      _spawnPipe();
    }

    // Copia de la lista para poder eliminar tuberías mientras se recorre.
    for (final pipe in List<_Pipe>.of(_pipes)) {
      pipe.update(step);

      if (pipe.x + _pipeWidth < 0) {
        pipe.remove();
        _pipes.remove(pipe);
        continue;
      }

      final birdRect = _birdRect();
      if (hayColision(personaje: birdRect, obstaculo: _rect(pipe.top)) ||
          hayColision(personaje: birdRect, obstaculo: _rect(pipe.bottom))) {
        _endGame();
        return;
      }

      if (!pipe.scored && pipe.x + _pipeWidth < _bird.position.x) {
        pipe.scored = true;
        _score++;
        onScoreChanged(_score);
      }
    }
  }

  Rect _birdRect() => Rect.fromLTWH(
        _bird.position.x,
        _bird.position.y,
        _birdWidth,
        _birdHeight,
      );

  Rect _rect(RectangleComponent component) => Rect.fromLTWH(
        component.position.x,
        component.position.y,
        component.size.x,
        component.size.y,
      );

  void _spawnPipe() {
    final minGapTop = 40.0;
    final maxGapTop = size.y - _groundHeight - _gapHeight - 40.0;
    final top = minGapTop +
        _random.nextDouble() * (maxGapTop > minGapTop ? maxGapTop - minGapTop : 1);

    _pipes.add(_Pipe(
      x: size.x,
      gapTop: top,
      groundY: size.y - _groundHeight,
    ));
    addAll(_pipes.last.components);
  }

  void _endGame() {
    _isGameOver = true;
    onGameOver();
  }

  // Vuelve a colocar el personaje y limpia los obstáculos.
  void restart() {
    for (final pipe in _pipes) {
      pipe.remove();
    }
    _pipes.clear();
    _score = 0;
    _isGameOver = false;
    _verticalSpeed = 0;
    _spawnTimer = _spawnInterval;
    _bird.position.setValues(size.x * 0.22, size.y * 0.4);
    onScoreChanged(0);
  }
}

// Par de rectángulos (arriba y abajo) que forman un obstáculo.
class _Pipe {
  _Pipe({required this.x, required double gapTop, required double groundY}) {
    top = RectangleComponent(
      position: Vector2(x, 0),
      size: Vector2(_pipeWidth, gapTop),
      paint: Paint()..color = const Color(0xFF65D186),
    );
    bottom = RectangleComponent(
      position: Vector2(x, gapTop + _gapHeight),
      size: Vector2(_pipeWidth, groundY - gapTop - _gapHeight),
      paint: Paint()..color = const Color(0xFF65D186),
    );
  }

  late final RectangleComponent top;
  late final RectangleComponent bottom;
  double x;
  bool scored = false;

  List<RectangleComponent> get components => [top, bottom];

  void update(double dt) {
    x -= _pipeSpeed * dt;
    top.position.x = x;
    bottom.position.x = x;
  }

  void remove() {
    top.removeFromParent();
    bottom.removeFromParent();
  }
}
