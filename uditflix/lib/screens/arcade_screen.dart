import 'package:flame/game.dart';
import 'package:flutter/material.dart';

import '../arcade/flappy_game.dart';

// Pantalla de UDIT Arcade: integra el juego de Flame dentro de la app
// y muestra la puntuación y el estado de Game Over con widgets Flutter.
class ArcadeScreen extends StatefulWidget {
  const ArcadeScreen({super.key});

  @override
  State<ArcadeScreen> createState() => _ArcadeScreenState();
}

class _ArcadeScreenState extends State<ArcadeScreen> {
  late final UditFlappyGame _game;
  int _puntuacion = 0;
  bool _gameOver = false;

  @override
  void initState() {
    super.initState();
    _game = UditFlappyGame(
      onScoreChanged: (puntuacion) {
        if (mounted) setState(() => _puntuacion = puntuacion);
      },
      onGameOver: () {
        if (mounted) setState(() => _gameOver = true);
      },
    );
  }

  void _reiniciar() {
    _game.restart();
    setState(() => _gameOver = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('UDIT Arcade'),
      ),
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Toca para saltar',
                    style: TextStyle(fontWeight: FontWeight.w600),
                  ),
                  Chip(
                    avatar: const Icon(Icons.star, color: Color(0xFFFFC857)),
                    label: Text('Puntos: $_puntuacion'),
                  ),
                ],
              ),
            ),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(16),
                  child: Stack(
                    children: [
                      GameWidget(game: _game),
                      if (_gameOver)
                        Positioned.fill(
                          child: AbsorbPointer(
                            child: Container(
                              color: const Color(0xAA202A33),
                              alignment: Alignment.center,
                              child: Card(
                                child: Padding(
                                  padding: const EdgeInsets.all(24),
                                  child: Column(
                                    mainAxisSize: MainAxisSize.min,
                                    children: [
                                      const Text(
                                        'GAME OVER',
                                        style: TextStyle(
                                          color: Color(0xFF38566B),
                                          fontSize: 28,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                      const SizedBox(height: 8),
                                      Text(
                                        'Puntuación: $_puntuacion',
                                        style: const TextStyle(
                                          color: Color(0xFF586570),
                                        ),
                                      ),
                                      const SizedBox(height: 20),
                                      ElevatedButton.icon(
                                        onPressed: _reiniciar,
                                        icon: const Icon(Icons.restart_alt),
                                        label: const Text('Reiniciar partida'),
                                        style: ElevatedButton.styleFrom(
                                          backgroundColor: Theme.of(context)
                                              .colorScheme
                                              .primary,
                                          foregroundColor: Colors.white,
                                        ),
                                      ),
                                    ],
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
            ),
          ],
        ),
      ),
    );
  }
}
