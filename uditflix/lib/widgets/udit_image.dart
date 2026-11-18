import 'package:flutter/material.dart';

// Imagen de cartelera reutilizable en el catálogo y en la ficha.
// Si la URL falla (por ejemplo, sin conexión) mostramos un icono.
class UditImage extends StatelessWidget {
  const UditImage({super.key, required this.url, this.fit = BoxFit.cover});

  final String? url;
  final BoxFit fit;

  @override
  Widget build(BuildContext context) {
    final source = url;
    if (source == null || source.isEmpty) {
      return const Center(
        child: Icon(Icons.movie_outlined, color: Color(0xFF71808B)),
      );
    }

    return Image.network(
      source,
      fit: fit,
      errorBuilder: (context, error, stackTrace) => const Center(
        child: Icon(Icons.broken_image_outlined, color: Color(0xFF71808B)),
      ),
      loadingBuilder: (context, child, loadingProgress) {
        if (loadingProgress == null) return child;
        return const Center(
          child: CircularProgressIndicator(strokeWidth: 2),
        );
      },
    );
  }
}
