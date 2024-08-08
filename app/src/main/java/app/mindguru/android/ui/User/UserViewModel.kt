package app.mindguru.android.ui.User

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mindguru.android.MainActivityViewModel
import app.mindguru.android.components.JavaUtils
import app.mindguru.android.data.model.User
import app.mindguru.android.data.repository.FirebaseRepository
import app.mindguru.android.data.repository.PreferenceRepository
import com.google.protobuf.Internal.BooleanList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val app: Application,
    private val firebaseRepository: FirebaseRepository,
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {
    private val _processing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val processing
        get() = _processing.asStateFlow()
    private val _loginSuccess: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loginSuccess
        get() = _loginSuccess.asStateFlow()
    private val _error = mutableStateOf(false)
    val error = _error.value

    fun updateProcessingState(isProcessing: Boolean) {
        _processing.value = isProcessing
    }
    fun loginWithToken(token: String, rawNonce: String) = viewModelScope.launch {
        val authResult = firebaseRepository.loginToFirebase(token)
        if (authResult) {
            firebaseRepository.setUpAccount()
            _loginSuccess.value = true
        } else {
            _error.value = true
            _processing.value = false
        }
    }

    fun loginWithEmailAndPass() = viewModelScope.launch {
        val id = JavaUtils.generateDeviceId().replace("-", "_")
        val pass = JavaUtils.computeMD5Hash(id)
        val email = "guest$id@mindguru.app"
        val authResult = firebaseRepository.loginToFirebase(email.trim(), pass)
        if (authResult) {
            firebaseRepository.setUpAccount()
            _loginSuccess.value = true
            preferenceRepository.setIsGuest(true)
        }
    }

    fun updateUserDetails(name: String, gender:String, dob: String, relationship: String, country: String, employment: String) {
        val user = User.currentUser
        if (user != null) {
            user.let {
                user.name = name
                user.gender = gender
                user.dob = dob
                user.relationship = relationship
                user.country = country
                user.employment = employment
                user.symptoms = ""
            }
            firebaseRepository.updateUser(user)
        }
    }

    fun updateSymptoms(symptoms: String) {
        val user = User.currentUser
        if (user != null) {
            user.symptoms = symptoms
            firebaseRepository.updateUser(user)
        }
    }

    fun setSeverity(severity: String) {
        preferenceRepository.setHealthSeverity(severity)
    }

}