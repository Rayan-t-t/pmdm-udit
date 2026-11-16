//datos simulados en esta primera version no tenemos
//una appi ni una base de datos
//Por eso utilizamos una lis de objetos creado manualmente

//Mas adelante sustituiremos estos datos por información
//que recebiremos desde el servidor mediante una API REST
import '../models/contenido.dart';

//Lista de cintenidos que aparecera en nuetra app
//cada elemento de la lisat es un objeto de  tipo contenido

const List<Contenido> contenidos = [
  Contenido(
    id: 1,
    titulo: 'Introduccion a flutter',
    descripcion: 'Aprende a crear aplicaciones multiplataforma',
    tipo: 'video',
    duracion: '15 minutos',
  ),
  Contenido(
    id: 2,
    titulo: 'El mundo de los comics',
    descripcion: 'Muchos comics y demasiadas letras',
    tipo: 'comic',
    duracion: '1500 paginas',
  ),
  Contenido(
    id: 3,
    titulo: 'Adios',
    descripcion: 'Historia lineal triste y aburrida',
    tipo: 'videojuego',
    duracion: '15 horas',
  ),
  Contenido(
    id: 4,
    titulo: 'Musica',
    descripcion: 'No escuches esto',
    tipo: 'Musica',
    duracion: '3.1416 minutos',
  ),
];
