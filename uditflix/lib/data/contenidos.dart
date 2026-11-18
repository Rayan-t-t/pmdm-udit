//datos simulados en esta primera version no tenemos
//una appi ni una base de datos
//Por eso utilizamos una lis de objetos creado manualmente

//Mas adelante sustituiremos estos datos por información
//que recebiremos desde el servidor mediante una API REST
import '../models/contenido.dart';

// Vídeos públicos de demostración (comprobados con una petición HTTP).
// El de MDN es un vídeo CC0 muy corto; el de W3C es un tráiler.
const String videoDemo = 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4';
const String videoDemoTrailer = 'https://media.w3.org/2010/05/sintel/trailer.mp4';

//Lista de cintenidos que aparecera en nuetra app
//cada elemento de la lisat es un objeto de  tipo contenido

const List<Contenido> contenidos = [
  Contenido(
    id: 1,
    titulo: 'Introduccion a flutter',
    descripcion: 'Aprende a crear aplicaciones multiplataforma',
    tipo: 'video',
    duracion: '15 minutos',
    imagenUrl: 'https://picsum.photos/seed/uditflix1/400/600',
    videoUrl: videoDemo,
    destacado: true,
  ),
  Contenido(
    id: 2,
    titulo: 'El mundo de los comics',
    descripcion: 'Muchos comics y demasiadas letras',
    tipo: 'comic',
    duracion: '1500 paginas',
    imagenUrl: 'https://picsum.photos/seed/uditflix2/400/600',
    videoUrl: videoDemoTrailer,
    destacado: true,
  ),
  Contenido(
    id: 3,
    titulo: 'Adios',
    descripcion: 'Historia lineal triste y aburrida',
    tipo: 'videojuego',
    duracion: '15 horas',
    imagenUrl: 'https://picsum.photos/seed/uditflix3/400/600',
    videoUrl: videoDemo,
  ),
  Contenido(
    id: 4,
    titulo: 'Musica',
    descripcion: 'No escuches esto',
    tipo: 'Musica',
    duracion: '3.1416 minutos',
    imagenUrl: 'https://picsum.photos/seed/uditflix4/400/600',
    videoUrl: videoDemo,
  ),
];
