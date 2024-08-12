// SettingsScreen.kt
package app.mindguru.android.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.mindguru.android.R
import app.mindguru.android.ui.components.ToolBar

@Composable
fun SettingsScreen(navigateBack : () -> Unit = {}, viewModel: SettingsViewModel = hiltViewModel()) {
    val times  by viewModel.notificationTimes.collectAsState()
    var is8AMChecked by remember { mutableStateOf(times.contains("08:00:00")) }
    var is1PMChecked by remember { mutableStateOf(times.contains("13:00:00")) }
    var is8PMChecked by remember { mutableStateOf(times.contains("20:00:00")) }

    Column(Modifier.fillMaxSize()) {
        ToolBar(title = "Settings", navigateBack = navigateBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Notification Settings", style = MaterialTheme.typography.displaySmall)
            Text("Select times of the day to receive notifications")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = is8AMChecked, onCheckedChange = { is8AMChecked = it })
                Text("8 AM - Daily")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = is1PMChecked, onCheckedChange = { is1PMChecked = it })
                Text("1 PM - Daily")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = is8PMChecked, onCheckedChange = { is8PMChecked = it })
                Text("8 PM - Daily")
            }
            Button(onClick = {
                val selectedTimes = mutableListOf<String>()
                if (is8AMChecked) selectedTimes.add("08:00:00")
                if (is1PMChecked) selectedTimes.add("13:00:00")
                if (is8PMChecked) selectedTimes.add("20:00:00")
                viewModel.setNotificationTimes(selectedTimes)
                navigateBack()
            }) {
                Text("Save")
            }
        }
    }
}