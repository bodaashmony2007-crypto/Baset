package com.example.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "enhancement_history")
data class EnhancementHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalUri: String,
    val enhancedPath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val sharpness: Float,
    val noiseReduction: Float,
    val scale: Int,
    val analysis: String,
    val enhancementSummary: String
)

@Dao
interface EnhancementHistoryDao {
    @Query("SELECT * FROM enhancement_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<EnhancementHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: EnhancementHistoryItem)

    @Query("DELETE FROM enhancement_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM enhancement_history")
    suspend fun clearAllHistory()
}

@Database(entities = [EnhancementHistoryItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): EnhancementHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "image_enhancer_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
