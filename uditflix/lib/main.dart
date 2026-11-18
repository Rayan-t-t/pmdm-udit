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
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF38566B),
          surface: const Color(0xFFFFFFFF),
        ),
        scaffoldBackgroundColor: const Color(0xFFF6F8FA),
        cardTheme: CardThemeData(
          color: Colors.white,
          elevation: 1,
          margin: EdgeInsets.zero,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
            side: const BorderSide(color: Color(0xFFE6EAEE)),
          ),
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.white,
          foregroundColor: Color(0xFF202A33),
          centerTitle: false,
          elevation: 0,
        ),
      ),
      home: const CatalogoScreen(),
    );
    
  }
}
