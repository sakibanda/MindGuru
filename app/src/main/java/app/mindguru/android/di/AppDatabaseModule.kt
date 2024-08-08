// app/src/main/java/app/mindguru/android/di/AppDatabaseModule.kt
package app.mindguru.android.di

import android.content.Context
import androidx.room.Room
import app.mindguru.android.data.sources.local.AppDatabase
import app.mindguru.android.data.sources.local.AppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideAppDao(appDatabase: AppDatabase): AppDao {
        return appDatabase.userDao()
    }
}