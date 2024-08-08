package app.mindguru.android.ui.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.util.Locale

@Preview(showBackground = true)
@Composable
fun PromptBar(status:String = "", onSend: (String) -> Unit = {}) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var hasFocus by remember { mutableStateOf(false) }
    val isKeyBoardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    //Clear text field focus on hide keyboard
    /*if (isKeyBoardVisible.not()) {
        focusManager.clearFocus()
    }*/
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            prompt = text?.get(0) ?: ""
        }
    }


    Row(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()) {
        Box(Modifier.weight(1f)){
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState -> hasFocus = focusState.hasFocus }
                    .padding(end = 8.dp),
                placeholder = { Text("Enter your message") }
            )
            //Mic icon
            if (isKeyBoardVisible.not() && prompt.isEmpty()) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
                    onClick = { /*Fill Text field with text from Google STT*/
                        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                            Toast.makeText(
                                context,
                                "Speech Recognition not available",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@IconButton
                        } else {
                            //Start Speech Recognition
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            intent.putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                            launcher.launch(intent)
                        }
                    }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }

        if(status == "PROCESSING") {
            CircularProgressIndicator(modifier = Modifier.size(50.dp).align(Alignment.CenterVertically))
        } else {
            Button(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(0.dp)
                    .size(55.dp)
                    .align(Alignment.CenterVertically),
                contentPadding = PaddingValues(0.dp),
                enabled = status != "PROCESSING",
                onClick = {
                    if(prompt.trim().isNotEmpty()) {
                        focusManager.clearFocus()
                        onSend(prompt.trim())
                        prompt = ""
                    }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}