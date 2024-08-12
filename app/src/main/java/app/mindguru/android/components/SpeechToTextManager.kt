package app.mindguru.android.components

import app.mindguru.android.ui.chat.ChatViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import androidx.core.app.ActivityCompat
import com.konovalov.vad.silero.VadSilero
import com.konovalov.vad.silero.config.FrameSize
import com.konovalov.vad.silero.config.Mode
import com.konovalov.vad.silero.config.SampleRate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class SpeechToTextManager<Call> @Inject constructor() {
    private val TAG = "SpeechToTextManager"

    val grpcClient = SpeechToTextGrpcClient()

    var transcript = ""
    var isInitialized = false

    private var _amplitude: MutableStateFlow<Int> = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    private var audioRecorder: AudioRecord? = null

    private lateinit var viewModel: ChatViewModel
    private lateinit var langugage: String
    private val sampleRate = 16000 // Sample rate in Hz
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var isRecording = false
    private var frameSize = 512

    // Note: currently, the Speech SDK support 16 kHz sample rate, 16 bit samples, mono (single-channel) only.
    private var af = AudioFormat.Builder()
        .setSampleRate(sampleRate)
        .setEncoding(audioFormat)
        .setChannelMask(channelConfig)
        .build()

    private var bufferSize = 2 * frameSize
    private var audioBuffer = byteArrayOf()

    lateinit var vad: VadSilero
    private var forceAzure = false
    private var isAzure = false
    private var timeout = System.currentTimeMillis()

    private var socketTimeout = System.currentTimeMillis()
    private var socketBuffer = byteArrayOf()

    lateinit var onSendClick: (String) -> Unit

    fun init(
        onSendClick: (String) -> Unit,
        viewModel: ChatViewModel,
        stopService: (Boolean) -> Unit
    ) {
        this.onSendClick = onSendClick
        Logger.d(TAG, "init")
        try {
            timeout = System.currentTimeMillis()
            CoroutineScope(Dispatchers.IO).launch {
                delay(600000)//600000
                if (System.currentTimeMillis() - timeout >= 600000) {
                    stopService(false)
                }
            }
            this.isAzure = false
            this.viewModel = viewModel
            this.langugage = "en"
            //Logger.d("SpeechToTextManager", "Language: $langugage")

            if (ActivityCompat.checkSelfPermission(
                    viewModel.app,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (!isInitialized) {
                    audioRecorder = AudioRecord.Builder()
                        .setAudioSource(MediaRecorder.AudioSource.MIC)
                        .setAudioFormat(af)
                        .setBufferSizeInBytes(bufferSize)
                        .build()

                    vad = VadSilero(
                        viewModel.app,
                        sampleRate = SampleRate.SAMPLE_RATE_16K,
                        frameSize = FrameSize.FRAME_SIZE_512,
                        mode = Mode.NORMAL
                    )
                }

                // Start recording in a separate thread

            }
        } catch (e: Exception) {
            //e.printStackTrace()
            Remote.captureException(e)
        }
        isInitialized = true
    }

    fun stop() {
        Logger.d(TAG, "stop")
        isRecording = false

        if (audioRecorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            //webSocketClient.close()
            audioRecorder?.stop()
        }
    }

    fun close() {
        stop()
    }

    fun destroy() {
        Logger.d(TAG, "destroy")
        stop()
        Logger.d(TAG, "audioRecorder.release")
        CoroutineScope(Dispatchers.IO).launch {
            while (audioRecorder?.recordingState != AudioRecord.RECORDSTATE_STOPPED) {
                delay(100)
            }
            audioRecorder?.release()
        }
    }

    fun cancelSTT() {
        Logger.d(TAG, "cancelSTT")
        //callRequest.cancel()
        //webSocketClient.close()
    }

    suspend fun record(stopService: (Boolean) -> Unit) {
        Logger.d(TAG, "record")
        if (isRecording) return

        isRecording = true
        transcript = ""
        Logger.d(TAG, "bufferSize $bufferSize")
        audioBuffer = byteArrayOf()

        val buffer = ByteArray(bufferSize)
        var prevBuffer1: ByteArray? = null
        var prevBuffer2: ByteArray? = null
        var startSpeechTime = 0L
        var lastSpeechTime = 0L

        try {
            audioRecorder?.startRecording()
            Logger.d(TAG, "audioRecorder.startRecording")

            while (isRecording) {
                val readResult = audioRecorder?.read(buffer, 0, buffer.size)

                if (readResult == AudioRecord.ERROR_BAD_VALUE || readResult == AudioRecord.ERROR_INVALID_OPERATION) {
                    stopService(false)
                    Logger.d(TAG, "Error reading audio data $readResult")
                    return
                }

                if (vad.isSpeech(buffer)) {
                    if (startSpeechTime == 0L) {
                        startSpeechTime = SystemClock.elapsedRealtime()
                    } else {
                        lastSpeechTime = SystemClock.elapsedRealtime()
                    }
                } else if (lastSpeechTime > 0 && SystemClock.elapsedRealtime() - lastSpeechTime >= 2000) {
                    Logger.d(TAG, "End Speech")
                    isRecording = false
                    viewModel.sttProcessing(true)
                }

                if (startSpeechTime != 0L) {
                    if (prevBuffer1 != null) {
                        saveAudioData(prevBuffer1)
                        prevBuffer1 = null
                    }
                    if (prevBuffer2 != null) {
                        saveAudioData(prevBuffer2)
                        prevBuffer2 = null
                    }
                    saveAudioData(buffer)
                } else {
                    prevBuffer1 = prevBuffer2
                    prevBuffer2 = buffer
                }

                _amplitude.value = calculateAmplitude(buffer)
            }
            audioRecorder?.stop()

            if (startSpeechTime != 0L) {
                Logger.e("STTM", "transcript: $transcript")
                grpcClient.streamAudio(audioBuffer) { transcript ->
                    Logger.d(TAG, "gRPC: $transcript")
                    if (transcript.isNotEmpty()) {
                        onSendClick(transcript)
                    }
                    viewModel.sttProcessing(false)
                }
                /*
                grpcClient.streamAudio(socketBuffer) { transcript ->
                    Logger.e(TAG, "transcript: $transcript")
                    this.transcript += transcript
                }
                if (transcript != "") {
                    onSendClick(transcript)
                }
                viewModel.sttProcessing(false)*/
            }

        } catch (e: Exception) {
            Logger.d(TAG, e.message.toString())
            Remote.captureException(e)
        }
    }

    private fun calculateAmplitude(buffer: ByteArray): Int {
        //Logger.d(TAG, "calculateAmplitude")
        var sum = 0
        for (i in buffer.indices) {
            sum += abs(buffer[i].toInt())
        }
        return sum / buffer.size
    }

    private fun saveAudioData(buffer: ByteArray) {
        audioBuffer += buffer
        socketBuffer += buffer
        if (!isRecording || System.currentTimeMillis() - socketTimeout >= 200) {
            socketTimeout = System.currentTimeMillis()
            /*grpcClient.streamAudio(socketBuffer) { transcript ->
                Logger.e(TAG, "transcript: $transcript")
                this.transcript += transcript
            }*/
            socketBuffer = byteArrayOf()
        }
    }
}
