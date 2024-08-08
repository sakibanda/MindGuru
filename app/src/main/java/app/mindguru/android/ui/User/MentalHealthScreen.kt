package app.mindguru.android.ui.User

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import app.mindguru.android.MainActivityViewModel
import app.mindguru.android.R
import app.mindguru.android.ui.chat.ChatBubble
import app.mindguru.android.ui.components.ToolBar

@Composable
fun CallMentalHealthScreen(navigateNext:  () -> Unit, viewModel: UserViewModel = hiltViewModel(),
        mainActivityViewModel: MainActivityViewModel = hiltViewModel()
                           ) {
    MentalHealthScreen(navigateNext, { mainActivityViewModel.exitApp() }) { viewModel.setSeverity(it) }
}

@Composable
fun MentalHealthScreen(navigateNext: () -> Unit, exitApp: () -> Unit, setSeverity:  (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedSeverity by remember { mutableStateOf("") }

    Column {
        ToolBar(icon = R.drawable.app_icon)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ChatBubble(
                message = "Hi, I am MindGuru, your mind health assistant & well wisher. Can you let me know how serious is your mental health condition? Is it ...",
                isUserMessage = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                selectedSeverity = "Mild"
                setSeverity(selectedSeverity)
                navigateNext()
            }) {
                Text("Mild")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                setSeverity(selectedSeverity)
                selectedSeverity = "Moderate"
                navigateNext()
            }) {
                Text("Moderate")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                setSeverity(selectedSeverity)
                selectedSeverity = "Severe"
                showDialog = true
            }) {
                Text("Severe")
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("MindGuru") },
                text = {
                    Text("Please, consult a mental health doctor immediately. You may wish to continue using the app or exit now. But, you are advised to consult doctor first.")
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        navigateNext()
                    }
                    ) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    Button(onClick = { exitApp() }) {
                        Text("Exit")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MentalHealthScreen({}, {}, {})
}