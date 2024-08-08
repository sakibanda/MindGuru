package app.mindguru.android.data.sources.local
import androidx.room.Database
import androidx.room.RoomDatabase
import app.mindguru.android.data.model.User

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): AppDao
}