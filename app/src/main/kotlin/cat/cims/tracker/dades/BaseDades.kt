package cat.cims.tracker.dades

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SeguimentsDao {
    @Query("SELECT * FROM seguiments ORDER BY nom COLLATE NOCASE")
    fun seguiments(): Flow<List<Seguiment>>

    @Query("SELECT * FROM registres ORDER BY data")
    fun registres(): Flow<List<Registre>>

    @Query("SELECT * FROM seguiments")
    suspend fun totsElsSeguiments(): List<Seguiment>

    @Query("SELECT * FROM registres")
    suspend fun totsElsRegistres(): List<Registre>

    @Query("SELECT * FROM seguiments WHERE id = :id")
    suspend fun seguiment(id: Long): Seguiment?

    @Insert
    suspend fun afegeix(seguiment: Seguiment): Long

    @Update
    suspend fun actualitza(seguiment: Seguiment)

    @Delete
    suspend fun esborra(seguiment: Seguiment)

    @Insert
    suspend fun afegeix(registre: Registre): Long

    @Delete
    suspend fun esborra(registre: Registre)
}

/**
 * La base de dades del mòbil. De moment és l'única: quan hi hagi Supabase, Room continuarà sent la font principal i
 * l'app funcionarà sense connexió, com a municipis i Andana.
 */
@Database(entities = [Seguiment::class, Registre::class], version = 1, exportSchema = false)
abstract class BaseDades : RoomDatabase() {
    abstract fun dao(): SeguimentsDao

    companion object {
        @Volatile private var instancia: BaseDades? = null

        fun de(context: Context): BaseDades = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(context.applicationContext, BaseDades::class.java, "tracker.db")
                .build().also { instancia = it }
        }
    }
}
