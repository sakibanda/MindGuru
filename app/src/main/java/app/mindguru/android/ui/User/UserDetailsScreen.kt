package app.mindguru.android.ui.User

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import app.mindguru.android.data.model.User
import app.mindguru.android.ui.chat.ChatBubble
import app.mindguru.android.ui.components.clickOnce
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview(showBackground = true)
@Composable
fun UserDetailsScreenPreview() {
    UserDetailsScreen({}) { _,_, _, _, _, _ -> }
}

@Composable
fun CallUserDetailsScreen(navigateNext : () -> Unit, viewModel: UserViewModel = hiltViewModel()) {
    UserDetailsScreen(navigateNext = navigateNext) { name, gender, dob, relationship, employment, country ->
        viewModel.updateUserDetails(name, gender, dob, relationship, country, employment)
        navigateNext()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(
    navigateNext: () -> Unit,
    updateUserDetails: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf( if(User.currentUser != null) User.currentUser?.name!! else "") }
    val datePickerState = rememberDatePickerState()
    val dob = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""
    var gender by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var employment by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val countries = remember { mutableStateOf(getCountries()) }
    val genderOptions = listOf("Male", "Female", "Other")
    val relationshipOptions = listOf("Married", "Single", "In Relationship")
    val employmentOptions = listOf("Employed", "Business", "Unemployed")

    if (showDatePicker) {
        Popup(
            onDismissRequest = { showDatePicker = false },
            alignment = Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 64.dp)
                    .shadow(elevation = 4.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = true,
                    /*onDateSelected = { millis ->
                        dob = convertMillisToDate(millis)
                        showDatePicker = false
                    }*/
                )
            }
        }
    }
    LaunchedEffect(key1 = datePickerState.selectedDateMillis) {
        if (datePickerState.selectedDateMillis != null) {
            showDatePicker = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ChatBubble(message = "Hi, I need more details about you to help you better. If you don't wish to provide you can skip to next step.", isUserMessage = false)

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        //Gender Type
        DropdownMenuField(
            label = "Gender",
            options = genderOptions,
            selectedOption = gender,
            onOptionSelected = { gender = it }
        )

        // Date of Birth
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            Text(text = "Date Of Birth:")
            Spacer(modifier = Modifier.weight(1f))
            Text(text = dob)
            IconButton(onClick = { showDatePicker = true }) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date Of Birth")
            }
        }

        // Relationship Type
        DropdownMenuField(
            label = "Relationship Type",
            options = relationshipOptions,
            selectedOption = relationship,
            onOptionSelected = { relationship = it }
        )

        // Employment Type
        DropdownMenuField(
            label = "Employment Type",
            options = employmentOptions,
            selectedOption = employment,
            onOptionSelected = { employment = it }
        )

        // Country
        DropdownMenuField(
            label = "Country",
            options = countries.value,
            selectedOption = country,
            onOptionSelected = { country = it }
        )

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navigateNext() }) {
                Text("Skip")
            }
            Button(onClick = {
                updateUserDetails(name, gender, dob, relationship, employment, country)
            }) {
                Text("Next")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DropdownMenuField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember {
        object : MutableInteractionSource {
            override val interactions = MutableSharedFlow<Interaction>(
                extraBufferCapacity = 16,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

            override suspend fun emit(interaction: Interaction) {
                when (interaction) {
                    is PressInteraction.Press -> {
                        expanded = true
                    }
                }

                interactions.emit(interaction)
            }

            override fun tryEmit(interaction: Interaction): Boolean {
                return interactions.tryEmit(interaction)
            }
        }
    }

    Box(Modifier
        .fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            interactionSource = interactionSource,
            modifier = Modifier
                .combinedClickable(
                    onClick = { expanded = true },
                    onDoubleClick = { expanded = true },
                )
                .fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }, text = {
                    Text(text = option)
                })
            }
        }
    }
}



fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun getCountries(): ArrayList<String> {
    val isoCountryCodes: Array<String> = Locale.getISOCountries()
    val countriesWithEmojis: ArrayList<String> = arrayListOf()
    for (countryCode in isoCountryCodes) {
        val locale = Locale("", countryCode)
        val countryName: String = locale.displayCountry
        val flagOffset = 0x1F1E6
        val asciiOffset = 0x41
        val firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset
        //val flag = (String(Character.toChars(firstChar)) + String(Character.toChars(secondChar)))
        countriesWithEmojis.add("$countryName")// $flag
    }
    return countriesWithEmojis
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
//Get Input from user details
fun Profile1() {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var employment by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dob = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""
    val textFieldModifier = Modifier
        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
        .padding(8.dp)
    val countries = remember { mutableStateOf(getCountries()) }

    Box(modifier = Modifier
        .pointerInput(Unit) {}
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)) {

        Column(Modifier
            .padding(16.dp)
            .pointerInput(Unit) {}
            .fillMaxSize()) {
            //Name
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                //Label
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                //TextField
                BasicTextField(value = "", onValueChange = { name = it }, modifier = textFieldModifier.width(200.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            //Date Of Birth
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                //Label
                Text(
                    text = "Date Of Birth",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = dob,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Row(Modifier.clickOnce{ showDatePicker = !showDatePicker }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Select")
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date Of Birth"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            //Relationship
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                //Label
                Text(
                    text = "Relationship Type",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                //TextField
                Row(Modifier.clickOnce{ showDatePicker = !showDatePicker }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Select")
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Select Relationship Type"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            //Employment
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                //Label
                Text(
                    text = "Employment Type",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                //TextField
                Row(Modifier.clickOnce{ showDatePicker = !showDatePicker }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Select")
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Select Employment Type"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            //Country
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                //Label
                Text(
                    text = "Country",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(Modifier.clickOnce{ showDatePicker = !showDatePicker }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Select")
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Select Country"
                    )
                }
                //TextField

            }
        }
    }

    if (showDatePicker) {
        Popup(
            onDismissRequest = { showDatePicker = false },
            alignment = Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 64.dp)
                    .shadow(elevation = 4.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        }
    }
}*/