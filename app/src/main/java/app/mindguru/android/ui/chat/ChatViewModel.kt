package app.mindguru.android.ui.chat

import android.app.Application
import android.content.Context
import android.icu.util.Calendar
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mindguru.android.components.Logger
import app.mindguru.android.components.Remote
import app.mindguru.android.components.Utils
import app.mindguru.android.data.model.User
import app.mindguru.android.data.repository.FirebaseRepository
import app.mindguru.android.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import net.fellbaum.jemoji.EmojiManager
import java.util.Locale
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
    val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status
    val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    val _mute = MutableStateFlow(preferenceRepository.getBoolean("mute", false))
    val mute: StateFlow<Boolean> = _mute

    private var tts: TextToSpeech? = null

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }

    private fun startTTS(message: String) {
        if (tts == null) {
            Logger.d(TAG, "TTS is null")
            return
        }
        var ttsContentClean = message
        var codeBlockRegex = Regex("""\n```([\s\S]*?)\n```""", RegexOption.MULTILINE)
        val codeBlockString = "Code Block"
        val inlineCodeRegex = Regex("""\n`([^`]*)\n`""")
        ttsContentClean = codeBlockRegex.replace(ttsContentClean) { " $codeBlockString." }
        ttsContentClean = inlineCodeRegex.replace(ttsContentClean) { " $codeBlockString." }
        codeBlockRegex = Regex("""^```([\s\S]*?)```""", RegexOption.MULTILINE)
        ttsContentClean = codeBlockRegex.replace(ttsContentClean) { " $codeBlockString." }
        ttsContentClean = EmojiManager.removeAllEmojis(ttsContentClean)
        ttsContentClean = ttsContentClean.replace(Regex("""[`*#_~\-]"""), " ")

        if (_mute.value) {
            Logger.d(TAG, "TTS is muted")
            return
        }
        tts?.speak(ttsContentClean, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun stopTTS() {
        tts?.stop()
    }

    private fun getLocaleBasedOnDeviceCountry(context: Context): Locale {
        val country = context.resources.configuration.locales[0].country
        //get language
        val language = context.resources.configuration.locales[0].language
        return Locale(language, country)
    }

    //PROCESSING, COMPLETED and ERRORED
    init {
        tts = TextToSpeech( app ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = getLocaleBasedOnDeviceCountry(app)
                tts!!.language = locale
            } else {
                tts = null
            }
        }

        viewModelScope.launch {
            try {
                firebaseRepository.getMessages().addSnapshotListener { snapshot, _ ->
                    if(snapshot == null || snapshot.isEmpty) {
                        _loading.value = false
                        if (User.currentUser != null){
                            val time = Calendar.getInstance().time
                            val context = "[[CONTEXT FROM THIS PROMPT ONLY: Time now is $time. First, greet patient based on day part of the time.]] "
                            sendMessage(context + Utils.getFirstPrompt())
                        }
                        return@addSnapshotListener
                    }
                    snapshot.let {
                        if(_loading.value) _loading.value = false
                        _messages.value = it.documents.map { doc ->
                            doc.data ?: emptyMap()
                        }
                        //Logger.d(TAG, "Messages: ${_messages.value}")
                        if (_messages.value.isNotEmpty()) {
                            val previousStatus = _status.value
                            _messages.value.last().let { doc ->
                                //status is hashmap with state
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
                            //_messages.value = _messages.value.drop(1)
                            if(previousStatus == "PROCESSING" && _status.value == "COMPLETED" && !_mute.value) {
                                startTTS(_messages.value.last()["response"] as String)
                            }
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

    fun resetChat() {
        viewModelScope.launch {
            try {
                firebaseRepository.resetChat()
            }catch (e: Exception) {
                Logger.e(TAG, "Error resetting chat: " + e.message.toString())
                Remote.captureException(e)
            }
        }
    }

    fun toggleMute() {
        _mute.value = !_mute.value
        preferenceRepository.setBoolean("mute", _mute.value)
        if(_mute.value) {
            stopTTS()
        }
        Logger.d(TAG, "Mute: ${_mute.value}")
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