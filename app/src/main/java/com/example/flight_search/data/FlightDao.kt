package com.example.flight_search.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    // 1. Sugerencias de autocompletar (busca por IATA o nombre, ordenado por pasajeros)
    @Query("""
        SELECT * FROM airport 
        WHERE iata_code LIKE '%' || :query || '%' 
        OR name LIKE '%' || :query || '%' 
        ORDER BY passengers DESC
    """)
    fun getAirportsByQuery(query: String): Flow<List<Airport>>

    // 2. Vuelos posibles (todos los aeropuertos excepto el de salida, ordenados por pasajeros)
    @Query("SELECT * FROM airport WHERE iata_code != :departureCode ORDER BY passengers DESC")
    fun getAllDestinations(departureCode: String): Flow<List<Airport>>

    // 3. Obtener todos los favoritos
    @Query("SELECT * FROM favorite")
    fun getAllFavorites(): Flow<List<Favorite>>

    // 4. Guardar un favorito
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    // 5. Eliminar un favorito
    @Delete
    suspend fun deleteFavorite(favorite: Favorite)
}