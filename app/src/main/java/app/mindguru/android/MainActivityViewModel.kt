package app.mindguru.android

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mindguru.android.data.repository.FirebaseRepository
import app.mindguru.android.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel@Inject constructor(
    private val app: Application,
    private val firebaseRepository: FirebaseRepository,
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {
    fun getSeverity(): String {
        return preferenceRepository.getHealthSeverity()
    }

    fun isGuestMode(): Boolean {
        return preferenceRepository.isGuest()
    }

    fun exitApp(){
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        app.startActivity(intent)
        System.exit(0)
    }

    fun setupUser() = viewModelScope.launch {
        firebaseRepository.setUpAccount()
    }
}