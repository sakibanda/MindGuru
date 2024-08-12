package app.mindguru.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.mindguru.android.R
import app.mindguru.android.components.SpeechToTextManager
import kotlin.math.round


@Composable
fun MicAnimation(speechToTextManager: SpeechToTextManager<Any?>) {
    val amplitude by speechToTextManager.amplitude.collectAsState()
    //Log.i("AMAZEAI", "amplitude: $amplitude")

    Box(
        contentAlignment = Center,
        modifier = Modifier
            .padding(10.dp)
            .size(40.dp)
    ) {
        // Waves
        val rippleRadius = (round(amplitude.toDouble() / 10) * 10).toInt() * 0.075f

        Box(
            Modifier
                .size(50.dp)
                .align(Center)
                .graphicsLayer {
                    scaleX = rippleRadius / 2
                    scaleY = rippleRadius / 2
                },
        ) {
            Box(
                Modifier
                    .size(50.dp)
                    .background(color = MaterialTheme.colorScheme.background, shape = CircleShape)
            )
        }

        // Mic icon
        Box(
            Modifier
                .size(50.dp)
                .align(Center)
                .background(color = MaterialTheme.colorScheme.background, shape = CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_mic_24),
                "",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(40.dp)
                    .align(Center)
            )
        }

    }

}