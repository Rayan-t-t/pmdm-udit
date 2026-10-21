package com.example.e_commerce_adaptativo

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedButton

//ACTIVITY PRINCIPAL
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            //material theme :aplica los colores y
            //tipografias por defecto
            MaterialTheme() {
                //SURFACE: el lienzo de fondo que ocupa la pantalla
                Surface(modifier = Modifier.fillMaxSize())
                {
                    PatallaPrincipal()
                }


            }
        }
    }
    //modelo de datos
// el molde que define que informacion tiene cada producto
    data class Producto(

        val nombre : String,
        val precio : String,
        val imanResId : Int, // el identificador de la imagen en res/drawble
        val link : String
    )
    //datos de prueba (Hardcodeados)
//De momento vive qui mismo, en el codigo.No vienen de ningun servidor ni base de datos
    val catalogoHamburguesas = listOf(
        Producto(
            "Wolverine","79.99 €",
            R.drawable.wolverine,"https://www.youtube.com/watch?v=s3pDMUWlA6I"
        ),
        Producto(
            "Spiderman 2","79.90 €",
            R.drawable.spiderman2 ,"https://www.youtube.com/watch?v=nq1M_Wc4FIc"
        ),
        Producto(
            "Bloodborne","29.90 €",
            R.drawable.bloodborne ,"https://www.youtube.com/watch?v=G203e1HhixY"
        ),
        Producto(
            "ARC Raiders","39.90 €",
            R.drawable.arcraiders ,"https://www.youtube.com/watch?v=U0jK145mxec"
        ),
        Producto(
            "Fallout 76","44.00 €",
            R.drawable.fallout76 ,"https://www.youtube.com/watch?v=psArWgV7tzQ"
        ),
        Producto(
            "Darkest Dungeon 2","42.30 €",
            R.drawable.darkestdungeon2 ,"https://www.youtube.com/watch?v=xjR_c5-RsoA"
        ),
    )
    //catalogo
//LAZY colum: pinta solo que pued erecorrer el scroll
//vertical solo dibuja en memora lo que s eve en patantalla
    @Composable
    fun CatalogoHamburguesa(productos: List<Producto>){
        /*  LazyColumn (
              modifier = Modifier.fillMaxSize(),
              // margen alrededor de toda la lista
              contentPadding = PaddingValues(16.dp),
              //espacio entre una tarjeta y la siguiente
              verticalArrangement = Arrangement.spacedBy(16.dp)
          )

         */
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            items(productos){producto ->
                TarjetaProducto(producto)
            }g
        }
    }
    //TARJETA DE PRODUCTO
// una caja(card) con imagen arriba y datos mas el boton
    @Composable
    fun TarjetaProducto(producto: Producto){
        //CARD: una supeficie elevada con sombra y bordes redondeados
        // por defecto  - ideal para agrupar visuamlmente la info de un producto
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            //column apila suselemtos de arriba a abajo(flexbox)
            Column() {
                Image(painterResource(
                    id =producto.imanResId
                ),
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxWidth()// esto ocupa todo el ancho de la tarajeta
                        .height(180.dp),//alto fijo "Se rompe al rotar la pantalla"
                    contentScale = ContentScale.Fit//Recorta la imagen sin deformacion
                )
                //segunda column, con margen ineterior para el texto y el boton
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = producto.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp)) //hueco pequeño

                    Text(
                        text = producto.precio,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val context = LocalContext.current
                        Button(
                            onClick = {
                                val url = producto.link
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Trailer")
                        }

                        // Botón 2: Comprar
                        Button(
                            onClick = {

                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Comprar")
                        }
                    }
                }

            }
        }
    //Pantalla de portada
    @Composable
    fun PatallaPrincipal() {
//remenber + mutable stateoff cre una variable que con compose vigila
        // es decir que cuando su valor cambia compose vuelve a dibujar la pantalla solo
        // sin qu tengas que hacer nada
        //launcheffect lanza una tarea que se ejecuta una sola vez
        // cuando la patalla aprece. debe espeara 1.5 seg
        var mostrarPortada by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            delay(1500)
            mostrarPortada = false

        }
        if (mostrarPortada) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("PS app", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            CatalogoHamburguesa(productos = catalogoHamburguesas)
        }


    }
}

