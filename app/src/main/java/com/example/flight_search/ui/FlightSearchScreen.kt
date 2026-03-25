package com.example.flight_search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flight_search.data.Airport

@Composable
fun FlightSearchScreen(viewModel: FlightViewModel) {
    val searchInput by viewModel.searchInput.collectAsState()
    val selectedAirport by viewModel.selectedAirport.collectAsState()
    val airportList by viewModel.airportList.collectAsState()
    val destinationList by viewModel.destinationList.collectAsState()
    val favoritesList by viewModel.favoritesList.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 1. Buscador
        OutlinedTextField(
            value = searchInput,
            onValueChange = { viewModel.updateSearchInput(it) },
            label = { Text("Busca un aeropuerto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Lógica para mostrar las listas
        when {
            // CASO A: Hay un aeropuerto seleccionado -> Mostrar posibles destinos
            selectedAirport != null -> {
                Text(
                    text = "Vuelos desde ${selectedAirport!!.iataCode}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(destinationList) { destination ->
                        // Verificamos si esta ruta exacta ya está en favoritos
                        val isFavorite = favoritesList.any {
                            it.departureCode == selectedAirport!!.iataCode &&
                                    it.destinationCode == destination.iataCode
                        }

                        FlightRouteCard(
                            departureCode = selectedAirport!!.iataCode,
                            departureName = selectedAirport!!.name,
                            destinationCode = destination.iataCode,
                            destinationName = destination.name,
                            isFavorite = isFavorite,
                            onFavoriteClick = {
                                if (isFavorite) {
                                    val favToRemove = favoritesList.first {
                                        it.departureCode == selectedAirport!!.iataCode &&
                                                it.destinationCode == destination.iataCode
                                    }
                                    viewModel.removeFavorite(favToRemove)
                                } else {
                                    viewModel.addFavorite(selectedAirport!!.iataCode, destination.iataCode)
                                }
                            }
                        )
                    }
                }
            }

            // CASO B: El buscador NO está vacío pero no hay aeropuerto seleccionado -> Mostrar sugerencias
            searchInput.isNotEmpty() -> {
                LazyColumn {
                    items(airportList) { airport ->
                        AirportSuggestionItem(airport = airport, onClick = {
                            viewModel.onAirportSelected(airport)
                        })
                    }
                }
            }

            // CASO C: El buscador está vacío -> Mostrar favoritos
            else -> {
                if (favoritesList.isEmpty()) {
                    Text("No tienes vuelos favoritos aún.", modifier = Modifier.padding(top = 16.dp))
                } else {
                    Text(
                        text = "Vuelos Favoritos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn {
                        items(favoritesList) { favorite ->
                            FlightRouteCard(
                                departureCode = favorite.departureCode,
                                departureName = "Salida", // Según requerimientos, favoritos solo guarda códigos
                                destinationCode = favorite.destinationCode,
                                destinationName = "Llegada",
                                isFavorite = true,
                                onFavoriteClick = { viewModel.removeFavorite(favorite) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTES VISUALES SECUNDARIOS ---

@Composable
fun AirportSuggestionItem(airport: Airport, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = airport.iataCode, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = airport.name)
    }
}

@Composable
fun FlightRouteCard(
    departureCode: String,
    departureName: String,
    destinationCode: String,
    destinationName: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Salida", style = MaterialTheme.typography.labelMedium)
                Text(text = "$departureCode - $departureName", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Llegada", style = MaterialTheme.typography.labelMedium)
                Text(text = "$destinationCode - $destinationName", fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Botón de Favorito",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}