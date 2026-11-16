import 'package:flutter/material.dart';

import 'screens/catalogo_screen.dart';

//Punto de entrada de aplicacion
void main() {
  runApp(const MyApp());
}

//Widget principal de uditflix

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      //Nombre de nuestra app
      title: 'UDITFLIX',
      //OCULTAMOS LA ETIQUETA DEBUG
      debugShowCheckedModeBanner: false,
      //Configuracion visual general
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF5B2D90)),
        scaffoldBackgroundColor: const Color(0xFFF7F5FA),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF5B2D90),
          foregroundColor: Colors.white,
          centerTitle: false,
        ),
      ),
      home: const CatalogoScreen(),
    );
    
  }
}
