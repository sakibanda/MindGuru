package app.mindguru.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mindguru.android.R

@Preview(showBackground = true)
@Composable
fun ToolBarPreview() {
    ToolBar(icon = 0, navigateBack = {},
        menuOptions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.ArrowOutward, contentDescription = "Back")
            }
        })
}

@Composable
fun ToolBar(
    title: String = stringResource(R.string.app_name),
    icon: Int = 0,
    navigateBack: (() -> Unit)? = null,
    menuOptions: (@Composable RowScope.() -> Unit)?  = null) {
    Row(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (navigateBack != null) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(10.dp)) // Placeholder for back button space
        }

        Row(if (navigateBack == null) Modifier.weight(1f) else Modifier, verticalAlignment = Alignment.CenterVertically) {
            if(icon != 0) {
                Spacer(Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp)) // Placeholder for icon space
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            if(icon != 0) {
                Spacer(Modifier.weight(1f))
            }
        }

        if (menuOptions != null) {
            Spacer(Modifier.weight(1f)) // Placeholder for menu options space
            Row {
                menuOptions()
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier
            .height(1.dp)
            .padding(bottom = 5.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    )
}