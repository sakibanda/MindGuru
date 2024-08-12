package app.mindguru.android.di

import app.mindguru.android.components.SpeechToTextManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SpeechToTextManagerModule {

    @Provides
    @Singleton
    fun provideSpeechToTextManager(): SpeechToTextManager<Any> {
        return SpeechToTextManager()
    }
}