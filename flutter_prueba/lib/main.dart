import 'package:flutter/material.dart';

//main() el puntoa de arranque de cualquier app flutter
//siempre existe un runApp() = onCreate()en Kotlin
void main(){
  runApp(const BurguerShopApp());
}

//APP RAÍZ
class BurguerShopApp extends StatelessWidget {
  //Const +super key es nuestro constructor - flutter pide siempre ese comando
  //No hace
  const BurguerShopApp({super.key});

  //Build es el metodo que todo widget debe tener.AQUI
  //es donde dibujas todo lo que se ve en la pantalla.
  @override
  Widget build(BuildContext context) {

    //MaterialApp: el "Contenedor "Raíz de toda la app ==>MainACtivity en kotlin
    return MaterialApp(
      debugShowCheckedModeBanner: false,//Quita la cinta roja
      title: 'BurguerShop',
      theme: ThemeData(
        //Color de la app(manipular los colores de la app)
        // Esta es la unica linea para cambiar el aspecto
        colorSchemeSeed: Colors.deepOrange,
        useMaterial3: true,
      ),
      home: const PantallaCatalogo(),

      //La primera pantalla que se ve al abrir el app
    );
  }
}

//Modelo de Datos
// En dart no existe "Data class" como en kotlin:una clase normal
//con un constructor que recibe los campos para hacer el mismo trabajo
class Producto{
  //final: el valor no cambia una vez creado el objeto(val en kotlin)
  final String nombre;
  final String precio;//Al modificar al carro de compras modificamos la variable
  final String imagenAsset;

  //Constructor:"Requiered" obliga a pasar siempre estos tres datos al crear un producto
  Producto({
    required this.nombre,
    required this.precio,
    required this.imagenAsset,
  });
}

//Datos de prueba
//Los mismo productos en kotlin, apuntado a los mismos archivos que tenemos en nuestros assets
//final List <producto> una lista que no se puede reasignar pero el contenido si
final List<Producto>catalogoHamburguesa =[
  Producto(nombre: 'ArcRaiders',
      precio: '7,90 €',
      imagenAsset:'assets/images/arcraiders.png'),

  Producto(nombre: 'Bloodborne Pc',
      precio: '7,90 €',
      imagenAsset:'assets/images/bloodborne.png'),

  Producto(nombre: 'Darkest Dungeon 2',
      precio: '7,90 €',
      imagenAsset:'assets/images/darkestdungeon2.png'),
  Producto(nombre: 'Fallout 76',
      precio: '7,90 €',
      imagenAsset:'assets/images/fallout76.png'),
  Producto(nombre: 'Spiderman 2',
      precio: '7,90 €',
      imagenAsset:'assets/images/spiderman2.png'),
  Producto(nombre: 'Wolverine',
      precio: '7,90 €',
      imagenAsset:'assets/images/wolverine.png'),
];

//Pantalla 1: Cagtalogo Responsive(Reto2)
class PantallaCatalogo extends StatelessWidget{
  const PantallaCatalogo({super.key});

  @override
  Widget build(BuildContext context){
    //Scafold la estructura estandar de una pantalla Android y IOS
    return Scaffold(
      //Barra superior con el titulo
      appBar: AppBar(title:const Text('🍔 BurguerShop')),
      //layoutBuilder: nos da el ancho disponible de la pantalla
      body: LayoutBuilder(
        builder:(context, constraints){
          const double anchoMinimo =250;
          const double espacio = 16;
          final double anchoUtil = constraints.maxWidth -(espacio * 2 );
          int columnas =((anchoUtil + espacio)/(anchoMinimo + espacio)).floor();
          if (columnas< 1)columnas = 1; //como minimo una columna
          return GridView.builder(
            padding: const EdgeInsets.all(espacio),//margen alrededor de la rejilla
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: columnas,
              mainAxisSpacing: espacio,
              crossAxisSpacing: espacio,
              mainAxisExtent: 340,
            ),
            itemCount: catalogoHamburguesa.length,
            itemBuilder:(context, index){
              final producto= catalogoHamburguesa[index];
              return TarjetaProducto(producto: producto);
            },
          );
        },
      ),
    );
  }
}
// Tarjeta de producto
class TarjetaProducto extends StatelessWidget {
  final Producto producto; // este es el dato importante para dibujar

  const TarjetaProducto({super.key, required this.producto});

  @override
  Widget build(BuildContext context) {
    // Card: es una caja con sombra y bordes redondeados por defecto
    // Es igual a nuestro Card de Compose
    return Card(
      margin: EdgeInsets.zero, // el hueco entre las tarjetas
      clipBehavior: Clip.antiAlias, // Para redondear las esquinas
      // Column apila elementos de arriba a abajo
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start, // alinea el texto a la izquierda
        children: [
          // Expanded: la imagen ocupa todo el alto
          Expanded(
            child: Image.asset(
              producto.imagenAsset,
              width: double.infinity, // ocupa todo el ancho disponible
              fit: BoxFit.cover, // Esto es igual al contentScale.crop en kotlin: recorta sin deformar
            ),
          ),
          // Segunda Column
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  producto.nombre,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4), // Spacer (height) para darle espacio (hueco pequeño)
                Text(
                  producto.precio,
                  style: TextStyle(
                    fontSize: 16,
                    color: Theme.of(context).colorScheme.primary, // Color del tema actual
                  ),
                ),
                const SizedBox(height: 8),
                SizedBox(
                  width: double.infinity, // el boton ocupa todo el ancho
                  child: ElevatedButton(
                    onPressed: () {
                      // de momento no hace nada pero en consola dira producto añadido al carrito
                      debugPrint('Añadido : ${producto.nombre}');
                    },
                    child: const Text('Añadir'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
} 