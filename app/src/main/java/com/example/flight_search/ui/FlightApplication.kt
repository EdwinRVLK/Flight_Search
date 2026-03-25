package com.example.flight_search

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.flight_search.data.FlightDatabase
import com.example.flight_search.data.UserPreferencesRepository

// Instanciamos el DataStore al nivel más alto
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "flight_preferences"
)

class FlightApplication : Application() {
    // Usamos 'lazy' para que se creen solo cuando se necesiten por primera vez
    val database: FlightDatabase by lazy { FlightDatabase.getDatabase(this) }
    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(dataStore)
    }
}