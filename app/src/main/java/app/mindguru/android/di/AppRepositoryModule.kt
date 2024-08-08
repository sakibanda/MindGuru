// app/src/main/java/app/mindguru/android/di/AppRepositoryModule.kt
package app.mindguru.android.di

import app.mindguru.android.data.repository.UserRepository
import app.mindguru.android.data.sources.local.AppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppRepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(appDao: AppDao): UserRepository {
        return UserRepository(appDao)
    }
}