//Importamos flutter para utilizar sus componentes visuales
import 'package:flutter/material.dart';

//importamos la lisat de contenidos simulados
import '../data/contenidos.dart';

//creamos la pantalla de catálogo

class CatalogoScreen extends StatelessWidget {
  //Constructor de la pantalla
  const CatalogoScreen({super.key});

  //metodo que construye la interfaz visual
  @override
  Widget build(BuildContext context) {
    //Scaffold proporciona la estrcutura basica de una pantalla.
    return Scaffold(
      //Barra superiro de la aplicación
      appBar: AppBar(
        //texto que aprece en la barra superio
        title: const Text('UDITflix'),
        //Color de fondo
        backgroundColor: Colors.deepPurpleAccent,
        //color de texto y de los iconos de barra.
        foregroundColor: Colors.white,
      ),
      //listView.builder crea una lista desplazable
      body: ListView.builder(
        //añadir un margen interio de 16 px
        padding: const EdgeInsets.all(16),
        itemCount: contenidos.length,
        itemBuilder: (context, index) {
          final contenido = contenidos[index];
          //Card crea una tarjete visual
          return Card(
            //Listtitle organiza el contenido de la tarjeta
            child: ListTile(
              leading: const Icon(
                Icons.play_circle_fill,
                color: Colors.deepPurpleAccent,
              ),
              //Mostratmos el titulo
              title: Text(contenido.titulo),
              subtitle: Text(contenido.tipo),
              trailing: const Icon(Icons.arrow_back_ios),
            ),
          );
        },
      ),
    );
  }
}
