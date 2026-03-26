package com.example.flight_search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flight_search.ui.FlightSearchScreen
import com.example.flight_search.ui.FlightViewModel
import com.example.flight_search.ui.theme.Flight_SearchTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Flight_SearchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Inicializamos el ViewModel usando la Factory que creamos
                    val viewModel: FlightViewModel = viewModel(factory = FlightViewModel.Factory)

                    // Llamamos a nuestra pantalla principal
                    FlightSearchScreen(viewModel = viewModel)
                }
            }
        }
    }
}