package app.mindguru.android.ui.components



import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mindguru.android.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Loading(modifier:Modifier = Modifier, enabled: Boolean = true) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
        .pointerInput(Unit) {}
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(50.dp)
                .size(150.dp)
                .background(
                    MaterialTheme.colorScheme.onBackground,
                    RoundedCornerShape(10.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Logo and text with gradient
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 10.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "App Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(
                        R.string.app_name
                    ),
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 15.sp,
                    style = TextStyle(
                        fontWeight = FontWeight.W400,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.basicMarquee()
                )

            }
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier
                    .then(Modifier.size(32.dp)),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            //Animate "..." text
            var text by remember {
                mutableLongStateOf(0)
            }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(500)
                    text++
                    if (text > 3)
                        text = 0
                }
            }
            Text(
                text = buildAnnotatedString {
                    append("Please wait")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.background
                        )
                    ) {
                        append(".".repeat(text.toInt()))
                    }
                    withStyle(
                        SpanStyle(
                            color = Color.Transparent
                        )
                    ) {
                        append(".".repeat(3-text.toInt()))
                    }
                },
                color = MaterialTheme.colorScheme.background,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}