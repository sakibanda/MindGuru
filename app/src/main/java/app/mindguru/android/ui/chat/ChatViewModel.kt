package app.mindguru.android.ui.chat

import android.app.Application
import android.content.Context
import android.icu.util.Calendar
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.fellbaum.jemoji.EmojiManager
import java.util.Locale
import javax.inject.Inject

const val TAG = "ChatViewModel"
@HiltViewModel
class ChatViewModel @Inject constructor(
    val app: Application,
    private val firebaseRepository: FirebaseRepository,
    val preferenceRepository: PreferenceRepository,
): ViewModel()/*, VoiceChatService.SpeechRecognitionListener*/ {
    private val _messages = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val messages: StateFlow<List<Map<String, Any>>> = _messages
    val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status
    val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    val _mute = MutableStateFlow(preferenceRepository.getBoolean("mute", false))
    val mute: StateFlow<Boolean> = _mute
    var tts: TextToSpeech? = null

    private val _sttProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val sttProcessing: StateFlow<Boolean> = _sttProcessing.asStateFlow()

    private var _isSpeaking: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    fun sttProcessing(value:Boolean){
        _sttProcessing.value = value
    }
    /*private val voiceChatService = VoiceChatService()

    fun startVoiceChat(languageCode: String) {
        try {
            voiceChatService.setSpeechRecognitionListener(this)
            voiceChatService.startVoiceChat(languageCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTranscriptReceived(transcript: String?) {
        if (transcript != null) {
            if (transcript.isNotEmpty()) {
                sendMessage(transcript)
            }
        }
    }

    fun stopVoiceChat() {
        voiceChatService.stopVoiceChat()
    }

    fun pauseVoiceChat() {
        voiceChatService.pauseRecognition()
    }

    fun resumeVoiceChat(languageCode: String) {
        try {
            voiceChatService.resumeRecognition(languageCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/

    fun setSpeaking(value: Boolean) {
        _isSpeaking.value = value
    }
    override fun onCleared() {
        stopTTS()
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
        _isSpeaking.value = true
        tts?.speak(ttsContentClean, TextToSpeech.QUEUE_FLUSH, null, (9999..99999).random().toString())
    }

    private fun stopTTS() {
        if(tts?.isSpeaking == true) {
            tts?.stop()
            _isSpeaking.value = false
        }
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
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    Logger.e(TAG, "TTS Stopped")
                    setSpeaking(false)
                    _isSpeaking.value = false
                }
                override fun onDone(utteranceId: String) {
                    Logger.e(TAG, "TTS Done")
                    setSpeaking(false)
                    _isSpeaking.value = false
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Logger.e(TAG, "TTS Error")
                    setSpeaking(false)
                    _isSpeaking.value = false
                }
                override fun onStart(utteranceId: String) {
                    Logger.e(TAG, "TTS Started")
                    setSpeaking(true)
                    _isSpeaking.value = true
                }
            })
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
                stopTTS()
                _messages.value = emptyList()
                _status.value = "PROCESSING"
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
                _status.value = "PROCESSING"
                stopTTS()
                firebaseRepository.sendMessage(prompt)
                messages.value.apply {
                    plus(mapOf("prompt" to prompt, "status" to "PROCESSING", "startTime" to System.currentTimeMillis()))
                }
            }catch (e: Exception) {
                Logger.e(TAG, "Error getting messages: " + e.message.toString())
                Remote.captureException(e)
            }
        }
    }
}