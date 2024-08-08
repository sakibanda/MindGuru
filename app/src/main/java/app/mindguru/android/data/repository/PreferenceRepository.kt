package app.mindguru.android.data.repository

import android.app.Application
import android.content.SharedPreferences
import javax.inject.Inject

class PreferenceRepository @Inject constructor( private val sharedPreferences: SharedPreferences,
                            private val app: Application
) {
    fun setHealthSeverity(severity: String) {
        sharedPreferences.edit().putString("healthSeverity", severity).apply()
    }
    fun getHealthSeverity(): String {
        return sharedPreferences.getString("healthSeverity", "") ?: ""
    }
    fun setIsGuest(isGuest: Boolean) {
        sharedPreferences.edit().putBoolean("isGuest", isGuest).apply()
    }
    fun isGuest(): Boolean {
        return sharedPreferences.getBoolean("isGuest", false)
    }
}