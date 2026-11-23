//MODELO CONTENIDO

// esta clase representa un contenido de nuestra plataforma
//Por ejemplo: una pelicula, un comic o un videojuego.

//en programcion orientada a objetos una clase definida
//que carecteristicas tendran los objetos que queremos
class Contenido {
  final int id;
  final String titulo;
  final String descripcion;
  final String tipo;
  final String duracion;

  // Campos opcionales para la cartelera (imagen), el reproductor (vídeo)
  // y la sección destacada. Son opcionales para no romper el catálogo
  // original, que solo tenía los cinco campos anteriores.
  final String? imagenUrl;
  final String? videoUrl;
  final bool destacado;

  // Constructor
  const Contenido({
    required this.id,
    required this.titulo,
    required this.descripcion,
    required this.tipo,
    required this.duracion,
    this.imagenUrl,
    this.videoUrl,
    this.destacado = false,
  });
}
