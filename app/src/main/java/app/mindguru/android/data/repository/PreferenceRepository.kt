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
    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    fun setInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    fun setLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

}