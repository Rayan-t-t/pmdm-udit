import 'package:flutter/material.dart';

import '../models/contenido.dart';
import '../widgets/udit_image.dart';
import 'reproductor_screen.dart';

// Ficha de detalle de un contenido del catálogo.
// Desde aquí se mantiene la navegación hacia el reproductor.
class FichaScreen extends StatelessWidget {
  const FichaScreen({super.key, required this.contenido});

  final Contenido contenido;

  void _reproducir(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => ReproductorScreen(contenido: contenido),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(contenido.titulo)),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            SizedBox(
              height: 260,
              child: Stack(
                children: [
                  Positioned.fill(
                    child: UditImage(url: contenido.imagenUrl),
                  ),
                  const Positioned.fill(
                    child: DecoratedBox(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [Colors.transparent, Color(0xFF202A33)],
                        ),
                      ),
                    ),
                  ),
                  Positioned(
                    left: 16,
                    bottom: 16,
                    right: 16,
                    child: Text(
                      contenido.titulo,
                      style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                  ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Wrap(
                    spacing: 8,
                    children: [
                      Chip(label: Text(contenido.tipo)),
                      Chip(label: Text(contenido.duracion)),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    contenido.descripcion,
                    style: Theme.of(context).textTheme.bodyLarge,
                  ),
                  const SizedBox(height: 24),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: () => _reproducir(context),
                      icon: const Icon(Icons.play_arrow),
                      label: const Text('Reproducir vídeo'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Theme.of(context).colorScheme.primary,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 14),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
