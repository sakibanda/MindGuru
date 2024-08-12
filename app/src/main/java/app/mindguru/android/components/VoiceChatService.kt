package app.mindguru.android.components


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import app.mindguru.android.R
import app.mindguru.android.ui.chat.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


const val INTENT_COMMAND = "COMMAND"
const val NOTIFICATION_CHANNEL_GENERAL = "General"

@AndroidEntryPoint
class VoiceChatService : Service() {
    private var running = true

    private var mBinder: IBinder = LocalBinder()

    private var _isRecording: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    lateinit var scope: CoroutineScope
    lateinit var viewModel: ChatViewModel
    @Inject
    lateinit var speechToTextManager: SpeechToTextManager<Any?>
    lateinit var mediaPlayer: MediaPlayer

    private lateinit var closeVoiceChat: () -> Unit

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.i("AMAZEAI", "OnTaskRemoved")
        super.onTaskRemoved(rootIntent)
        stopService()
    }

    fun init(
        onSendClick: (String) -> Unit,
        viewModel: ChatViewModel,
        closeVoiceChat: () -> Unit
    ) {
        Logger.e("VoiceChatService", "init")
        running = true
        this.viewModel = viewModel
        this.closeVoiceChat = closeVoiceChat
        speechToTextManager.init(onSendClick, viewModel) { stopService(it) }
        mediaPlayer = MediaPlayer.create(this, R.raw.alert)
    }

    fun cancelSTT() {
        speechToTextManager.cancelSTT()
    }

    fun startRecording() {//speechToTextManager : SpeechToTextManager
        Log.i("AMAZEAI", "startRecording")
        try {
            if (!_isRecording.value) {
                CoroutineScope(Dispatchers.IO).launch {
                    speechToTextManager.record { stopService(it) }
                }
                Thread.sleep(500L)
                _isRecording.value = true
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            Remote.captureException(e)
            //e.printStackTrace()
            Log.i("AMAZEAI", "startRecording: ${e.localizedMessage}")
        }
    }

    fun stopRecording() {//speechToTextManager : SpeechToTextManager
        Log.i("AMAZEAI", "stopRecording")
        try {
            if (_isRecording.value) {
                speechToTextManager.stop()
                mediaPlayer.start()
                _isRecording.value = false
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            Remote.captureException(e)
            Log.i("AMAZEAI", "stopRecording: ${e.localizedMessage}")
        }
    }

    override fun onDestroy() {
        Log.i("AMAZEAI", "onDestroy")
        try {
            super.onDestroy()
            if (running) {
                stopService()
            }
            speechToTextManager.destroy()
        } catch (e: Exception) {
            //e.printStackTrace()
            Remote.captureException(e)
            Log.i("AMAZEAI", "onDestroy: ${e.localizedMessage}")
        }
    }




    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("AMAZEAI", "onStartCommand")
        val command = intent.getStringExtra(INTENT_COMMAND)
        Log.i("AMAZEAI", "COMMAND: $command")
        if (command == "EXIT") {
            stopService()
            return START_NOT_STICKY
        }

        if (command == "APP") {
            return START_STICKY
        }

        showNotification()
        return START_STICKY
    }




    override fun onBind(intent: Intent?): IBinder {
        Log.i("AMAZEAI", "onBind")
        return mBinder
    }

    inner class LocalBinder : Binder() {
        fun getService(): VoiceChatService {
            Log.i("AMAZEAI", "getService")
            return this@VoiceChatService
        }

    }


    fun displayNotification() {
        Log.i("AMAZEAI", "displayNotification")
        val nMgr = this.applicationContext
            .getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nMgr.notify(999, notificationObj)
    }

    lateinit var notificationObj: Notification

    @SuppressLint("LaunchActivityFromNotification")
    private fun showNotification() {
        Log.i("AMAZEAI", "showNotification")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val appIntent = Intent(this, VoiceChatService::class.java).apply {
            putExtra(INTENT_COMMAND, "APP")
        }
        val appPendingIntent = PendingIntent.getService(
            this, 50, appIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val exitIntent = Intent(this, VoiceChatService::class.java).apply {
            putExtra(INTENT_COMMAND, "EXIT")
        }

        val exitPendingIntent = PendingIntent.getService(
            this, 51, exitIntent, PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_GENERAL,
                        "Voice Chat",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                ) {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    manager.createNotificationChannel(this)
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                Remote.captureException(e)
                Log.d("Error", "showNotification: ${e.localizedMessage}")
            }
        }

        with(
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_GENERAL)
        ) {
            setChannelId(NOTIFICATION_CHANNEL_GENERAL)
            setTicker(null)
            setContentTitle(applicationContext.getString(R.string.voice_chat))
            setContentText("")
            setAutoCancel(true)
            setOngoing(false)
            setWhen(System.currentTimeMillis())
            setSmallIcon(R.drawable.app_icon)
            priority = Notification.VISIBILITY_PUBLIC
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(appPendingIntent)
            addAction(
                NotificationCompat.Action(
                    0,
                    applicationContext.getString(R.string.stop).uppercase(),
                    exitPendingIntent
                )
            )

            notificationObj = build()
            startForeground(999, notificationObj)
        }
    }

    fun stopService(toast: Boolean = true) {
        closeVoiceChat()
        //if(!running) return
        CoroutineScope(Dispatchers.IO).launch {
            Log.i("AMAZEAI", "stopService")
            try {
                running = false
                mediaPlayer.release()
                _isRecording.value = false
                speechToTextManager.close()
                //speechToTextManager.destroy()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (e: Exception) {
                Log.i("AMAZEAI", "stopService: ${e.localizedMessage}")
                Remote.captureException(e)
                //e.printStackTrace()
            }
        }
        if (toast) Toast.makeText(
            this,
            this.getString(R.string.voice_chat_ended),
            Toast.LENGTH_LONG
        ).show()
    }
}


