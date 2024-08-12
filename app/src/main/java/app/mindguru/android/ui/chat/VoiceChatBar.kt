package app.mindguru.android.components.kotlin

import app.mindguru.android.ui.chat.ChatViewModel
import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.mindguru.android.R
import app.mindguru.android.components.Logger
import app.mindguru.android.components.Remote
import app.mindguru.android.components.VoiceChatService
import app.mindguru.android.ui.components.MicAnimation

@Composable
fun VoiceChatBar(
    closeVoiceChat: (() -> Unit) -> Unit,
    onSendClick: (String) -> Unit,
    viewModel: ChatViewModel,
    voiceChat: (Boolean) -> Unit,
) {
    Log.i("AMAZEAI", "InputVoiceBar")

    var isCanRecordPermissionGranted by remember { mutableStateOf(false) }
    var voiceChatService: VoiceChatService? by remember { mutableStateOf(null) } //DEVA
    var isPaused by remember { mutableStateOf(false) } //DEVA
    var isBackPressed by remember { mutableStateOf(false) } //DEVA
    val status by viewModel.status.collectAsState()
    val isAiProcessing = (status == "PROCESSING")
    val isSTTProcessing by viewModel.sttProcessing.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    var canRecord = false
    var isRecording = false
    val context = LocalContext.current

    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_DESTROY -> {
                    Log.i("AMAZEAI", "Lifecycle.Event.ON_DESTROY")
                    //speechToTextManager.shutDown(scope)//
                    voiceChatService?.stopService()
                    voiceChatService = null
                    //Toast.makeText(context, "Voice Chat Ended", Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose {
            lifecycle.lifecycle.removeObserver(observer)
        }
    }

    if (isBackPressed) {
        return
    }

    if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
        canRecord = true
    }

    if (!canRecord) {
        var permissionsToRequest = listOf<String>()
        permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            listOf(
                Manifest.permission.RECORD_AUDIO
            )
        }

        val permissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                Log.i("AMAZEAI", permissions.toString())

                if (permissions["android.permission.RECORD_AUDIO"] == true) {
                    isCanRecordPermissionGranted = true
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.voice_chat_allow_mic_perm),
                        Toast.LENGTH_LONG
                    ).show()

                    //rationale false show app setting
                    val rationaleNeeded = permissionsToRequest.any { permission ->
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            context as ComponentActivity,
                            permission
                        )
                    }
                    Log.i("AMAZEAI", "rationaleNeeded = $rationaleNeeded")
                    if (!rationaleNeeded) {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.packageName)
                        )
                        context.startActivity(intent)
                    }
                    voiceChat(false)
                }
            }
        LaunchedEffect(
            key1 = permissionLauncher,
            key2 = isCanRecordPermissionGranted,
            key3 = voiceChatService
        ) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
        return
    }

    if (voiceChatService == null) {
        foregroundStartService(context) {
            isRecording = true
            voiceChatService = it
            voiceChatService?.init(onSendClick, viewModel) {
                //if (isAiProcessing) viewModel.stopAIContentGeneration()
                if (isSpeaking) viewModel.tts?.stop()
                if (isSTTProcessing) voiceChatService?.cancelSTT()
                voiceChat(false)
            }
            voiceChatService?.startRecording()
            closeVoiceChat {
                voiceChatService?.stopService()
                voiceChatService = null
                voiceChat(false)
            }
        }

        Remote.setUserProperty("voice_chat", "true")
    }


    Log.i(
        "AMAZEAI",
        "isAiProcessing - isSpeaking - isSTTProcessing - isPaused: $isAiProcessing - $isSpeaking - $isSTTProcessing - $isPaused"
    )
    if (!isSTTProcessing && !isAiProcessing && !isSpeaking && !isPaused) {
        Log.i("AMAZEAI", "Begin Recording Setup")
        if (!isRecording)
            voiceChatService?.startRecording()
        isRecording = true
    } else {
        isRecording = false
        voiceChatService?.stopRecording()
    }
    //End Speech To Text

    //Main Row
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(65.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onBackground)
            //border bottom 1dp
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.background
            )
    ) {

        Spacer(modifier = Modifier.width(10.dp))
        //Close Button
        IconButton(
            onClick = {
                voiceChatService?.stopService()
                voiceChatService = null
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(50.dp)
                .background(
                    color = Color.Red,
                    shape = RoundedCornerShape(90.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(25.dp)
            )
        }
        //End

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            //Show Loading Animation
            var isLoadingVisible = false
            if (isAiProcessing || isSTTProcessing) {
                isLoadingVisible = true
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = isLoadingVisible,
                enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                exit = scaleOut(animationSpec = tween(durationMillis = 500)),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    /*Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,//thin
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                            trackColor = MaterialTheme.colorScheme.background,
                        )
                    }*/
                    // Mic Off icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MicOff,
                            contentDescription = "MicOff",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .size(35.dp)
                        )
                    }
                }
            }

            //Show Mic Animation
            var isMicVisible = false
            if (canRecord && isRecording) {
                isMicVisible = true
            }
            Logger.e("AMAZEAI", "isMicOffVisible: $isMicVisible")
            androidx.compose.animation.AnimatedVisibility(
                visible = isMicVisible,
                enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                exit = scaleOut(animationSpec = tween(durationMillis = 500)),
            ) {
                voiceChatService?.speechToTextManager?.let { MicAnimation(it) }
            }
            //Show Mic Off
            var isMicOffVisible = false
            if (!isLoadingVisible && !isMicVisible)
                isMicOffVisible = true
            Logger.e("AMAZEAI", "isMicOffVisible: $isMicOffVisible")
            androidx.compose.animation.AnimatedVisibility(
                visible = isMicOffVisible,
                enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                exit = scaleOut(animationSpec = tween(durationMillis = 500)),
            ) {
                Icon(
                    imageVector = if (canRecord) {
                        Icons.Filled.MicOff
                    } else Icons.Filled.Error,
                    contentDescription = "image",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }

        }

        if(isAiProcessing || isSTTProcessing){
            CircularProgressIndicator(modifier = Modifier.size(50.dp).align(Alignment.CenterVertically), color = MaterialTheme.colorScheme.background)
        }else {
            //Show Play Pause Error Buttons
            IconButton(
                onClick = {
                    if (isSpeaking) {
                        viewModel.tts?.stop()
                        viewModel.setSpeaking(false)
                    } else
                        isPaused = isPaused.not()
                },
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterVertically)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(90.dp)
                    )
            ) {
                Icon(
                    imageVector = if (canRecord.not()) Icons.Rounded.Settings else if (isSpeaking) Icons.Rounded.Stop else if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,//Pause Play Stop Permission
                    contentDescription = "Stop Play Pause",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
    }

}

fun foregroundStartService(
    context: Context,
    voiceChatServiceObject: (VoiceChatService) -> Unit
) {
    Log.i("AMAZEAI", "foregroundStartService")
    var voiceChatService: VoiceChatService? = null
    val intent = Intent(context, VoiceChatService::class.java)
    var mBounded = false

    val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Log.i("AMAZEAI", "onServiceDisconnected")
            //Toast.makeText(this@MainActivity, "Service is disconnected", Toast.LENGTH_SHORT).show()
            mBounded = false
            voiceChatService = null
        }

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.i("AMAZEAI", "onServiceConnected")
            Log.i("AMAZEAI", "voiceChatService: " + voiceChatService.toString())
            //Toast.makeText(this@MainActivity, "Service is connected", Toast.LENGTH_SHORT).show()
            mBounded = true
            voiceChatService = (binder as VoiceChatService.LocalBinder).getService()

            voiceChatService?.startService(intent)
            voiceChatServiceObject(voiceChatService!!)
        }
    }
    context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
}
