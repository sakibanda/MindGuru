package app.mindguru.android.components

import android.content.Context
import android.os.Bundle
import app.mindguru.android.BuildConfig
import com.android.billingclient.api.Purchase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Remote {
    companion object {


        fun log(message: String) {
            Logger.e("Remote", "logMessage")
            Firebase.crashlytics.log(message)
        }

        //chat_count image_count speech_count video_count total_count
        fun setUserProperty(name: String, value: String) {
            CoroutineScope(Dispatchers.IO).launch {
                Logger.e("Remote", "setUserProperty $name $value")
                try {
                    Firebase.analytics.setUserProperty(name, value)
                } catch (e: Exception) {
                    captureException(e)
                }
            }
        }

        fun checkGate(name: String, default: Boolean = true): Boolean {
            return getBoolean(name.uppercase())

        }

        fun getArray(key: String): List<String> {
            return try {
                Firebase.remoteConfig.getString(key).split(",")
            } catch (e: Exception) {
                captureException(e)
                emptyList()
            }
        }

        fun getIntArray(key: String): List<Int> {
            return try {
                Firebase.remoteConfig.getString(key).split(",").map { it.toInt() }
            } catch (e: Exception) {
                captureException(e)
                emptyList()
            }
        }

        fun getString(key: String, default: String = ""): String {

            return try {
                Firebase.remoteConfig.getString(key)
            } catch (e: Exception) {
                captureException(e)
                default.ifEmpty { key }
            }
        }

        fun getBoolean(key: String): Boolean {
            return try {
                Firebase.remoteConfig.getBoolean(key)
            } catch (e: Exception) {
                captureException(e)
                false
            }
        }

        fun getLong(key: String): Long {
            return try {
                Firebase.remoteConfig.getLong(key)
            } catch (e: Exception) {
                captureException(e)
                0
            }
        }

        fun getInt(key: String): Int {
            return try {
                Firebase.remoteConfig.getLong(key).toInt()
            } catch (e: Exception) {
                captureException(e)
                0
            }
        }

        fun captureMessage(e: Exception){
            Sentry.captureMessage(e.message ?: "Unknown error")
        }

        fun captureMessage(message: String){
            Logger.e("Remote", "captureMessage $message")
            try {
                Sentry.captureMessage(message)
                FirebaseCrashlytics.getInstance().log(message)
            } catch (e: Exception) {
                captureException(e)
            }
        }

        fun captureException(e: Throwable) {
            if (e.toString().contains("JobCancellationException", true)) return
            Logger.e("Remote", "captureException $e")
            try {
                if (BuildConfig.DEBUG || getBoolean("DEBUG"))
                    e.printStackTrace()
                Sentry.captureException(e)
                Firebase.crashlytics.recordException(e)
            } catch (e: Exception) {
                e.printStackTrace()
                Logger.e("Remote", "captureException catch $e")
            }
        }

        fun captureException(e: Exception) {
            if (e.toString().contains("JobCancellationException", true)) return
            Logger.e("Remote", "captureException $e")
            try {
                if (BuildConfig.DEBUG || getBoolean("DEBUG"))
                    e.printStackTrace()
                Sentry.captureException(e)
                Firebase.crashlytics.recordException(e)
            } catch (e: Exception) {
                e.printStackTrace()
                Logger.e("Remote", "captureException catch $e")
            }
        }

        fun logEventWithPurchase(name: String, context: Context, purchase: Purchase) {
            CoroutineScope(Dispatchers.IO).launch {
                Logger.d("Remote", "logEventWithPurchase $purchase")
                try {
                } catch (e: Exception) {
                    captureException(e)
                }
            }
        }

        fun logChatEvent(name: String, params: Map<String, String> = emptyMap()) {
            CoroutineScope(Dispatchers.IO).launch {
                Logger.d("Remote", "logChatEvent $name")
                try {
                } catch (e: Exception) {
                    captureException(e)
                }
            }
        }

        fun logEvent(
            name: String,
            context: Context? = null,
            value: String
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                Logger.d("Remote", "logEvent $name $value")
                try {
                } catch (e: Exception) {
                    captureException(e)
                }
                try {
                    if (context != null) {
                    }
                } catch (e: Exception) {
                    captureException(e)
                }
                try {
                    Firebase.analytics.logEvent(name, Bundle().apply {
                        putString(FirebaseAnalytics.Param.VALUE, value)
                    })
                } catch (e: Exception) {
                    captureException(e)
                }
            }
        }

        fun logEvent(
            name: String,
            context: Context? = null,
            params: Map<String, String> = emptyMap()
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                Logger.d("Remote", "logEvent $name $params")
                try {
                } catch (e: Exception) {
                    captureException(e)
                }
                try {
                    //BRANCH_STANDARD_EVENT.INVITE
                    if (context != null) {
                    }
                } catch (e: Exception) {
                    captureException(e)
                }

                try {
                    Firebase.analytics.logEvent(name, Bundle().apply {
                        params.forEach { (key, value) -> putString(key, value) }
                    })
                } catch (e: Exception) {
                    captureException(e)
                }
            }
        }
    }
}