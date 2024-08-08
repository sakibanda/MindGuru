package app.mindguru.android.ui.chat

import android.app.Application
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mindguru.android.components.Logger
import app.mindguru.android.components.Remote
import app.mindguru.android.data.model.User
import app.mindguru.android.data.repository.FirebaseRepository
import app.mindguru.android.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "ChatViewModel"
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val app: Application,
    private val firebaseRepository: FirebaseRepository,
    private val preferenceRepository: PreferenceRepository,
): ViewModel() {
    private val _messages = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val messages: StateFlow<List<Map<String, Any>>> = _messages
    val _status = MutableStateFlow<String>("")
    val status: StateFlow<String> = _status
    val _loading = MutableStateFlow<Boolean>(true)
    val loading: StateFlow<Boolean> = _loading

    //PROCESSING, COMPLETED and ERRORED

    init {
        viewModelScope.launch {
            try {
                firebaseRepository.getMessages().addSnapshotListener { snapshot, _ ->
                    if(snapshot == null || snapshot.isEmpty) {
                        _loading.value = false
                        if (User.currentUser != null){
                            //calculate age from  User.currentUser!!.dob
                            val age  = 2024 - User.currentUser!!.dob.drop(6).toInt()
                            val userProfile = "My name is ${User.currentUser!!.name}, " +
                                    "I am $age years old, ${User.currentUser!!.gender}. My profession is ${User.currentUser!!.employment} and I am from ${User.currentUser!!.country}."
                            val symptoms = "I am experiencing these ${User.currentUser!!.healthSeverity} symptoms: ${User.currentUser!!.symptoms}. "
                            val instructions = "Please provide me with the necessary help."
                            val time = Calendar.getInstance().time
                            val context = "[[CONTEXT: Time now is $time. First, greet patient based on day part of the time.]] "
                            sendMessage(context + userProfile + symptoms + instructions)
                        }
                        return@addSnapshotListener
                    }
                    snapshot.let {
                        if(_loading.value) _loading.value = false
                        _messages.value = it.documents.map { doc ->
                            doc.data ?: emptyMap()
                        }
                        Logger.d(TAG, "Messages: ${_messages.value}")
                        if (_messages.value.isNotEmpty()) _messages.value.last().let { doc ->
                            //status is hasmap with state
                            //check if doc["status"] is String
                            if (doc["status"] is String) {
                                _status.value = doc["status"] as String
                            } else if (doc["status"] is HashMap<*, *>) {
                                val statusMap = doc["status"] as HashMap<String, Any>
                                _status.value = statusMap["state"] as String? ?: ""
                                //_status.value = doc["status"] as String? ?: ""
                            }
                            //_status.value = doc["status"] as String? ?: ""
                        }
                        Logger.d(TAG, "Status: ${_status.value}")
                    }
                }
            }catch (e: Exception) {
                Logger.e(TAG, "Error getting messages: " + e.message.toString())
                Remote.captureException(e)
            }
        }
    }

    fun sendMessage(prompt: String) {
        viewModelScope.launch {
            try {
                firebaseRepository.sendMessage(prompt)
                messages.value.apply {
                    plus(mapOf("prompt" to prompt, "status" to "PROCESSING", "startTime" to System.currentTimeMillis()))
                }
                _status.value = "PROCESSING"
            }catch (e: Exception) {
                Logger.e(TAG, "Error getting messages: " + e.message.toString())
                Remote.captureException(e)
            }
        }
    }
}