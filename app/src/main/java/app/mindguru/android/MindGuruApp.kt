package app.mindguru.android

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.mindguru.android.components.Logger
import app.mindguru.android.components.Remote
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MindGuruApp: Application() {
    override fun onCreate() {
        //create OnBackInvokedCallback
        super.onCreate()

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    Logger.d("ProcessLifecycleOwner", "onStart")
                }

                override fun onStop(owner: LifecycleOwner) {
                    Logger.d("ProcessLifecycleOwner", "onStop")
                }
            })

        } catch (e: Exception) {
            Logger.e("AmazeAIApp", e.message.toString())
            Remote.captureException(e)
        }
    }
}