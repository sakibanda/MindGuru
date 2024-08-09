package app.mindguru.android.ui.Userimport

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import app.mindguru.android.ui.User.UserViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.mindguru.android.components.Logger
import app.mindguru.android.data.model.User
import app.mindguru.android.ui.chat.ChatBubble
import app.mindguru.android.ui.components.clickOnce

import my.nanihadesuka.compose.ColumnScrollbar

@Composable
fun ShowSymptomsScreen(navigateNext: () -> Unit, viewModel: UserViewModel = hiltViewModel()) {
    SymptomsScreen { symptoms ->
        viewModel.updateSymptoms(symptoms)
        navigateNext()
    }
}

@Preview (showBackground = true)
@Composable
fun SymptomsScreen(onSubmit: (String) -> Unit = {}) {
    val options = listOf("Anxiety or Persistent Sadness or Depression", "Social Withdrawal or Loneliness", "Hallucinations or Overthinking", "Addiction", "Mood Swings","Changes in Appetite or Weight",
        "Personal Issues or Problems", "Feelings of Guilt or Worthlessness", "Sleep Disturbances", "Career Counselling", "Concentration and Memory Problems")
    val selectedOptions = remember { mutableStateListOf<String>() }
    var customDetails by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val isKeyBoardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    LaunchedEffect(Unit) {
        if(User.currentUser != null) {
            selectedOptions.addAll(User.currentUser!!.symptoms.split(",").map { it.trim() })
        }
        if(!options.contains(selectedOptions.last())) {
            customDetails = selectedOptions.last()
            selectedOptions.remove(customDetails)
        }
        Logger.e("SymptomsScreen", "Symptoms: ${selectedOptions.toList()}")
    }

    LaunchedEffect(isKeyBoardVisible) {
        if (isKeyBoardVisible) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    ColumnScrollbar(scrollState) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatBubble(
                message = "To analyse your mental health please select your symptoms ...",
                isUserMessage = false
            )
            Spacer(modifier = Modifier.height(16.dp))

            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .padding(0.dp)
                        .height(35.dp)
                        .fillMaxWidth()
                        .clickOnce {
                            if (selectedOptions.contains(option))
                                selectedOptions.remove(option)
                            else
                                selectedOptions.add(option)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        modifier = Modifier
                            .padding(0.dp),
                        checked = selectedOptions.contains(option),
                        onCheckedChange = {
                            if (it) selectedOptions.add(option) else selectedOptions.remove(
                                option
                            )
                        }
                    )
                    Text(option, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Enter any other details:")
            OutlinedTextField(
                value = customDetails,
                onValueChange = { customDetails = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val symptoms =
                    (selectedOptions + customDetails.trim()).filter { it.isNotBlank() }
                        .joinToString(", ")
                if (symptoms == "")
                    error = true
                else
                    onSubmit(symptoms)
            }) {
                Text("Submit")
            }

            if (error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Please select at least one symptom or add more details",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}