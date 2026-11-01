package com.prem.daybyday.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "weight_entries")
data class WeightEntry(@PrimaryKey val date: String, val kilograms: Double, val updatedAt: Long = System.currentTimeMillis())

@Entity(tableName = "workout_sessions", indices = [Index("date"), Index("workoutType")])
data class WorkoutSession(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val workoutType: String, val createdAt: Long = System.currentTimeMillis())

@Entity(tableName = "workout_sets", foreignKeys = [ForeignKey(entity = WorkoutSession::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE)], indices = [Index("sessionId"), Index("exerciseName")])
data class WorkoutSet(@PrimaryKey(autoGenerate = true) val id: Long = 0, val sessionId: Long, val exerciseName: String, val setNumber: Int, val weightKg: Double, val reps: Int)

@Entity(tableName = "user_goals")
data class UserGoal(@PrimaryKey val id: Int = 1, val targetWeightKg: Double, val startWeightKg: Double, val createdAt: Long = System.currentTimeMillis())

@Dao interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY date ASC") fun observeAll(): Flow<List<WeightEntry>>
    @Query("SELECT * FROM weight_entries WHERE date = :date LIMIT 1") suspend fun get(date: String): WeightEntry?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entry: WeightEntry)
    @Query("DELETE FROM weight_entries WHERE date = :date") suspend fun delete(date: String)
}

@Dao interface WorkoutDao {
    @Insert suspend fun insertSession(session: WorkoutSession): Long
    @Insert suspend fun insertSets(sets: List<WorkoutSet>)
    @Transaction suspend fun saveWorkout(session: WorkoutSession, sets: List<WorkoutSet>) { val id = insertSession(session); insertSets(sets.map { it.copy(sessionId = id) }) }
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC, createdAt DESC") fun observeSessions(): Flow<List<WorkoutSession>>
    @Query("SELECT * FROM workout_sets WHERE exerciseName = :exercise ORDER BY id ASC") fun observeExercise(exercise: String): Flow<List<WorkoutSet>>
    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY setNumber ASC") suspend fun setsForSession(sessionId: Long): List<WorkoutSet>
    @Query("SELECT * FROM workout_sets WHERE exerciseName = :exercise ORDER BY id DESC LIMIT 3") suspend fun latestSets(exercise: String): List<WorkoutSet>
}

@Dao interface GoalDao {
    @Query("SELECT * FROM user_goals WHERE id = 1 LIMIT 1") fun observeGoal(): Flow<UserGoal?>
    @Query("SELECT * FROM user_goals WHERE id = 1 LIMIT 1") suspend fun getGoal(): UserGoal?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(goal: UserGoal)
}

@Database(entities = [WeightEntry::class, WorkoutSession::class, WorkoutSet::class, UserGoal::class], version = 2, exportSchema = false)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun goalDao(): GoalDao
    companion object {
        @Volatile private var INSTANCE: WeightDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS workout_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date TEXT NOT NULL, workoutType TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_sessions_date ON workout_sessions(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_sessions_workoutType ON workout_sessions(workoutType)")
                db.execSQL("CREATE TABLE IF NOT EXISTS workout_sets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sessionId INTEGER NOT NULL, exerciseName TEXT NOT NULL, setNumber INTEGER NOT NULL, weightKg REAL NOT NULL, reps INTEGER NOT NULL, FOREIGN KEY(sessionId) REFERENCES workout_sessions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_sets_sessionId ON workout_sets(sessionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_sets_exerciseName ON workout_sets(exerciseName)")
                db.execSQL("CREATE TABLE IF NOT EXISTS user_goals (id INTEGER NOT NULL PRIMARY KEY, targetWeightKg REAL NOT NULL, startWeightKg REAL NOT NULL, createdAt INTEGER NOT NULL)")
            }
        }
        fun get(context: Context): WeightDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, WeightDatabase::class.java, "prem-local.db").addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
        }
    }
}
