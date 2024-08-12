package app.mindguru.android.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import app.mindguru.android.R
import app.mindguru.android.components.Remote
import app.mindguru.android.components.kotlin.VoiceChatBar
import app.mindguru.android.ui.components.ConfirmationDialog
import app.mindguru.android.ui.components.Loading
import app.mindguru.android.ui.components.ToolBar
import app.mindguru.android.ui.theme.IconTint
import kotlinx.coroutines.delay
import my.nanihadesuka.compose.ColumnScrollbar

@Composable
fun ShowChatScreen(viewModel: ChatViewModel = hiltViewModel(), navController: NavHostController) {
    val messages by viewModel.messages.collectAsState(emptyList())
    val status by viewModel.status.collectAsState("")
    val loading by viewModel.loading.collectAsState(true)
    val mute by viewModel.mute.collectAsState(false)
    val view = LocalView.current
    var voiceChat by remember { mutableStateOf(false) } //DEVA
    var closeVoiceChat: () -> Unit = {}
    var notificationPermissionShown by remember { mutableStateOf(false) }
    val context = LocalContext.current
    if(loading){
        Loading()
        return
    }

    if (!notificationPermissionShown && !viewModel.preferenceRepository.getBoolean("notification_shown", false) && (context.checkSelfPermission(
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED/* || context.checkSelfPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED*/) && SDK_INT >= Build.VERSION_CODES.TIRAMISU
    ) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.POST_NOTIFICATIONS,
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        /*if(SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest += Manifest.permission.POST_NOTIFICATIONS
        }*/

        val permissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                /*if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true) {
                    Remote.logEvent("notification_permission", context, "granted")
                } else {
                    Remote.logEvent("notification_permission", context, "denied")
                }*/
            }
        ConfirmationDialog(
            image = R.drawable.notification_bell,
            isAlert = false,
            title = stringResource(id = R.string.notifications_permission_confirm_title),
            message = stringResource(id = R.string.notifications_permission_confirm_message),
            onCancel = {
                viewModel.preferenceRepository.setBoolean("notification_shown", true)
                notificationPermissionShown = true
                Remote.logEvent("notification_permission", context, "button_no")
            }) {
            viewModel.preferenceRepository.setBoolean("notification_shown", true)
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
            notificationPermissionShown = true
            Remote.logEvent("notification_permission", context, "button_yes")
        }
        Remote.logEvent("notification_permission", context, "shown")
    }

    ChatScreen(messages, status=status, sendMessage=viewModel::sendMessage, mute=mute, muteAction=viewModel::toggleMute,
        navigateToProfile = { navController.navigate("UserDetailsScreen") }, navigateToSymptoms = { navController.navigate("SymptomsScreen") },
        resetChat = viewModel::resetChat, voiceChat = voiceChat, setVoiceChat = {voiceChat = it},
        navigateToSettings = { navController.navigate("SettingsScreen") }, navigateToMoodChart = { navController.navigate("MoodTrackerScreen") }){
        VoiceChatBar(
            closeVoiceChat = { closeVoiceChat = it },
            onSendClick = { userText ->
                viewModel.sendMessage(userText)
            },
            viewModel = viewModel,
            ) {
                voiceChat = false
                viewModel.tts?.stop()
            }
    }

    // Ensure the insets are applied to the view
    DisposableEffect(Unit) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemInsets.bottom)
            insets
        }
        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
        }
    }
}

@Composable
fun ChatScreen(messages: List<Map<String, Any>> = emptyList(), status:String = "COMPLETED", sendMessage: (String) -> Unit = {}, mute: Boolean = false,
               muteAction: () -> Unit = {}, navigateToProfile: () -> Unit = {}, navigateToSymptoms: () -> Unit = {}, resetChat: () -> Unit = {}, voiceChat:Boolean = false,
                setVoiceChat: (Boolean) -> Unit = {},
                navigateToSettings: () -> Unit = {},
                navigateToMoodChart: () -> Unit = {},
               voiceChatComposable : @Composable () -> Unit  = { }) {
    val scrollState = rememberScrollState()
    val lazyListState = rememberLazyListState()

    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        ToolBar(
            icon = R.drawable.app_icon
        ){
            var expanded by remember { mutableStateOf(false) }
            IconButton(
                onClick = { muteAction() },
                modifier = Modifier
                    .padding(start = 9.dp, top = 3.dp, bottom = 3.dp, end = 0.dp)
                    .width(27.dp)
                    .height(27.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = if (mute) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
                    /*else if(!isSpeaking)  Icons.AutoMirrored.Outlined.VolumeUp
                    else (if(rippleEffect) Icons.AutoMirrored.Outlined.VolumeDown else Icons.AutoMirrored.Outlined.VolumeUp),*/
                    contentDescription = "Mute Button",
                    tint = if (mute) Color.Red else IconTint,
                    modifier = Modifier
                        .width(27.dp)
                        .height(27.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .padding(horizontal = 9.dp, vertical = 3.dp)
                    .width(27.dp)
                    .height(27.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .width(27.dp)
                        .height(27.dp)
                )
            }

            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(surface = MaterialTheme.colorScheme.background),
                shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(6.dp)),
            ) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.tertiary,
                            RoundedCornerShape(6.dp)
                        ),
                    properties = PopupProperties(focusable = false)
                ) {
                    DropdownMenuItem(
                        onClick = { navigateToProfile() },
                        text = {
                            Text(
                                text = "Edit Profile",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }, leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                "Edit Profile",
                                modifier = Modifier.size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    DropdownMenuItem(
                        onClick = { navigateToSymptoms() },
                        text = {
                            Text(
                                text = "Edit Symptoms",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }, leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                "Edit Symptoms",
                                modifier = Modifier.size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    )


                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )



                    DropdownMenuItem(
                        onClick = { navigateToMoodChart() },
                        text = {
                            Text(
                                text = "Mood Tracker",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }, leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                "Mood Tracker",
                                modifier = Modifier.size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    DropdownMenuItem(
                        onClick = { navigateToSettings() },
                        text = {
                            Text(
                                text = "Settings",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }, leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                "Settings",
                                modifier = Modifier.size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )



                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            resetChat() },
                        text = {
                            Text(
                                text = "Reset Chat",
                                color = Color.Red,
                                modifier = Modifier.padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }, leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                "Rename Conversation",
                                modifier = Modifier.size(25.dp),
                                tint = Color.Red,
                            )
                        }
                    )

                }
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomStart) {
            ColumnScrollbar(state = scrollState, padding = 2.dp, thickness = 4.dp) {
                Column(
                    modifier = Modifier
                        .imePadding()
                        .fillMaxSize()
                        .padding(10.dp)
                        .align(Alignment.BottomEnd)
                        .verticalScroll(scrollState)
                ) {
                    messages.forEachIndexed { index, message ->
                        if(index != 0)
                            ChatBubble(message = message["prompt"] as String, isUserMessage = true)
                        if(message["response"] != null) {
                            ChatBubble(
                                message = message["response"] as String,
                                isUserMessage = false
                            )
                        }
                    }
                    if(status == "PROCESSING") {
                        ChatBubble(message = "Processing...", isUserMessage = false, loading = true)
                    }
                }
            }
        }
        /*Box(Modifier.weight(1f), contentAlignment = Alignment.BottomStart) {
            ColumnScrollbar(state = scrollState, padding = 2.dp, thickness = 4.dp) {
                Column(
                    modifier = Modifier
                        .imePadding()
                        .fillMaxSize()
                        .padding(10.dp)
                        .align(Alignment.BottomEnd)
                        .verticalScroll(scrollState)
                ) {
                    messages.forEachIndexed { index, message ->
                        if(index != 0)
                            ChatBubble(message = message["prompt"] as String, isUserMessage = true)
                        if(message["response"] != null) {
                            ChatBubble(
                                message = message["response"] as String,
                                isUserMessage = false
                            )
                        }
                    }
                    if(status == "PROCESSING") {
                        ChatBubble(message = "Processing...", isUserMessage = false, loading = true)
                    }
                }
            }
        }*/

        HorizontalDivider()
        Box( contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxWidth()) {
            if (voiceChat) {
                    voiceChatComposable()
            } else{
                Row(
                    modifier = Modifier
                        .height(65.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onBackground)
                ) {
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = !voiceChat,
                enter = slideInVertically { it },
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 1000)
                )
            ) {
                PromptBar(status, voiceChat = {setVoiceChat(true)}, onSend = sendMessage )
            }
            //VoiceChatBar()
        }
    }

    LaunchedEffect(status) {
        delay(100)
        scrollState.scrollTo(scrollState.maxValue)
        if(status == "ERRORED") {
            Toast.makeText(context, "Error getting response from server", Toast.LENGTH_SHORT).show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen( listOf(
        mapOf("prompt" to "Hello", "response" to "Hi"),
        mapOf("prompt" to "How are you?", "response" to "I'm good, thank you!"),
        mapOf("prompt" to "What's your name?", "response" to "I'm an AI assistant"),
        mapOf("prompt" to "Hello", "response" to "Hi"),
        mapOf("prompt" to "How are you?", "response" to "I'm good, thank you!"),
        mapOf("prompt" to "What's your name?", "response" to "I'm an AI assistant"),
        mapOf("prompt" to "Hello", "response" to "Hi"),
        mapOf("prompt" to "How are you?", "response" to "I'm good, thank you!"),
        mapOf("prompt" to "What's your name?", "response" to "I'm an AI assistant"),
        mapOf("prompt" to "Hello", "response" to "Hi"),
        mapOf("prompt" to "How are you?", "response" to "I'm good, thank you!"),
        mapOf("prompt" to "What's your name?", "response" to "I'm an AI assistant"),
        mapOf("prompt" to "Hello", "response" to "Hi"),
        mapOf("prompt" to "How are you?", "response" to "I'm good, thank you!"),
        mapOf("prompt" to "What's your name?", "response" to "I'm an AI assistant"),
        mapOf("prompt" to "Hello", "response" to "Hi"),
        mapOf("prompt" to "How are you?", "response" to "I'm good, thank you!"),
        mapOf("prompt" to "What's your name?", "response" to "I'm an AI assistant"),
    ) )
}
