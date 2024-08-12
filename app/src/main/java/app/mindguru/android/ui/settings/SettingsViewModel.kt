package app.mindguru.android.ui.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import app.mindguru.android.data.repository.FirebaseRepository
import app.mindguru.android.data.repository.PreferenceRepository
import app.mindguru.android.utils.ScheduleNotifications
import app.mindguru.android.utils.WORK_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel@Inject constructor(
    val app: Application,
    private val firebaseRepository: FirebaseRepository,
    private val preferenceRepository: PreferenceRepository,
    ) : ViewModel() {

    private val _notificationTimes = MutableStateFlow<List<String>>(emptyList())
    val notificationTimes: StateFlow<List<String>> = _notificationTimes

    init {
        getNotificationTimes()
    }

    private fun getNotificationTimes() {
        val times = preferenceRepository.getString("notification_times", "08:00:00,13:00:00,20:00:00").split(",")
        _notificationTimes.value = times
    }

    fun setNotificationTimes(times: List<String>) {
        preferenceRepository.setString("notification_times", times.joinToString(","))
        cancelAllWork()
        ScheduleNotifications.schedule(app, times)
    }

    private fun cancelAllWork() {
        val workManager = WorkManager.getInstance(app)
        workManager.cancelAllWorkByTag(WORK_TAG)
    }
}