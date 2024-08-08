package app.mindguru.android.ui.chat

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.mindguru.android.R
import app.mindguru.android.ui.components.Loading
import app.mindguru.android.ui.components.ToolBar
import kotlinx.coroutines.delay
import my.nanihadesuka.compose.ColumnScrollbar

@Composable
fun CallChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    val status by viewModel.status.collectAsState(initial = "")
    val loading by viewModel.loading.collectAsState(initial = true)

    if(loading){
        Loading()
        return
    }
    ChatScreen(messages, status, viewModel::sendMessage)
}

@Composable
fun ChatScreen(messages: List<Map<String, Any>> = emptyList(), status:String, onSend: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        ToolBar(icon = R.drawable.app_icon)

        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomStart) {
            ColumnScrollbar(state = scrollState, padding = 2.dp, thickness = 4.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .align(Alignment.BottomEnd)
                        .verticalScroll(scrollState)
                ) {
                    messages.forEachIndexed { index, message ->
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

        HorizontalDivider()
        Box( contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxWidth()) {
            PromptBar(status, onSend = onSend )
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
    ), "") { }
}
