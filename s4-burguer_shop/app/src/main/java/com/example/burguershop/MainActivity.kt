
package com.example.burguershop

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.burguershop.ui.theme.BurguerShopTheme
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
                    //CatalogoHamburguesas(catalogoHamburguesas)
                }
            }


        }
    }
}
//modelo de datos
// el molde que define que informacion tiene cada producto
data class Producto(
    val nombre : String,
    val precio : String,
    val imanResId : Int // el identificador de la imagen en res/drawble
)
//datos de prueba (Hardcodeados)
//De momento vive qui mismo, en el codigo.No vienen de ningun servidor ni base de datos
val catalogoHamburguesas = listOf(
    Producto(
        "Burguer Clasica","6.50 €",
        R.drawable.burger_clasica
    ),
    Producto(
        "Burguer BBQ","8.50 €",
        R.drawable.burger_bbq
    ),
    Producto(
        "Burguer doble","7.90 €",
        R.drawable.burger_doble
    ),
    Producto(
        "Burguer Picante","10.50 €",
        R.drawable.burger_picante
    ),
    Producto(
        "Burguer pollo","6.50 €",
        R.drawable.burger_pollo
    ),
    Producto(
        "Burguer vegetarian","16.50 €",
        R.drawable.burger_vegetariana
    ),
)