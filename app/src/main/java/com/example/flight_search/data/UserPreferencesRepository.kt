package com.example.flight_search.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val SEARCH_VALUE = stringPreferencesKey("search_value")
        const val TAG = "UserPreferencesRepo"
    }

    // Flujo que emite el último valor de búsqueda guardado. Si no hay nada, emite un texto vacío ("")
    val searchValue: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error al leer las preferencias.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SEARCH_VALUE] ?: ""
        }

    // Función para guardar el nuevo texto de búsqueda cada vez que el usuario escribe
    suspend fun saveSearchValue(searchValue: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_VALUE] = searchValue
        }
    }
}