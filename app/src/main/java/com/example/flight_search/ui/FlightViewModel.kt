package com.example.flight_search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flight_search.FlightApplication
import com.example.flight_search.data.Airport
import com.example.flight_search.data.Favorite
import com.example.flight_search.data.FlightDao
import com.example.flight_search.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlightViewModel(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // 1. Guarda el texto que el usuario escribe en el buscador
    private val _searchInput = MutableStateFlow("")
    val searchInput: StateFlow<String> = _searchInput.asStateFlow()

    // 2. Guarda el aeropuerto si el usuario seleccionó uno de las sugerencias
    private val _selectedAirport = MutableStateFlow<Airport?>(null)
    val selectedAirport: StateFlow<Airport?> = _selectedAirport.asStateFlow()

    init {
        // Al iniciar, recuperamos la última búsqueda guardada en DataStore
        viewModelScope.launch {
            userPreferencesRepository.searchValue.collect { savedSearch ->
                if (_searchInput.value.isEmpty()) {
                    _searchInput.value = savedSearch
                }
            }
        }
    }

    // Se llama cada vez que el usuario teclea algo
    fun updateSearchInput(input: String) {
        _searchInput.value = input
        _selectedAirport.value = null // Reseteamos la selección porque está buscando de nuevo
        viewModelScope.launch {
            userPreferencesRepository.saveSearchValue(input) // Guardamos en DataStore
        }
    }

    // Se llama cuando el usuario toca un aeropuerto de la lista de sugerencias
    fun onAirportSelected(airport: Airport) {
        _selectedAirport.value = airport
        _searchInput.value = airport.iataCode
    }

    // --- FLUJOS DE DATOS REACTIVOS ---

    // Lista de sugerencias (se actualiza automáticamente al escribir)
    val airportList: StateFlow<List<Airport>> = _searchInput
        .flatMapLatest { query ->
            if (query.isNotBlank()) {
                flightDao.getAirportsByQuery(query)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vuelos de destino posibles (se activa cuando se selecciona un aeropuerto)
    val destinationList: StateFlow<List<Airport>> = _selectedAirport
        .flatMapLatest { airport ->
            if (airport != null) {
                flightDao.getAllDestinations(airport.iataCode)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lista de favoritos (siempre actualizada)
    val favoritesList: StateFlow<List<Favorite>> = flightDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- FUNCIONES PARA FAVORITOS ---

    fun addFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            flightDao.insertFavorite(Favorite(departureCode = departureCode, destinationCode = destinationCode))
        }
    }

    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch {
            flightDao.deleteFavorite(favorite)
        }
    }

    // Factory para que Android sepa cómo construir este ViewModel con sus dependencias
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightApplication)
                FlightViewModel(
                    flightDao = application.database.flightDao(),
                    userPreferencesRepository = application.userPreferencesRepository
                )
            }
        }
    }
}