package com.prem.daybyday.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "weight_entries")
data class WeightEntry(@PrimaryKey val date: String, val kilograms: Double, val updatedAt: Long = System.currentTimeMillis())

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY date ASC") fun observeAll(): Flow<List<WeightEntry>>
    @Query("SELECT * FROM weight_entries WHERE date = :date LIMIT 1") suspend fun get(date: String): WeightEntry?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entry: WeightEntry)
    @Query("DELETE FROM weight_entries WHERE date = :date") suspend fun delete(date: String)
}

@Database(entities = [WeightEntry::class], version = 1, exportSchema = false)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao
    companion object {
        @Volatile private var INSTANCE: WeightDatabase? = null
        fun get(context: Context): WeightDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, WeightDatabase::class.java, "prem-local.db").build().also { INSTANCE = it }
        }
    }
}
