package app.mindguru.android.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import app.mindguru.android.R


@Composable
fun ConfirmationDialog(
    closeButton: Boolean = false,
    isAlert: Boolean = false,
    confirmText: String? = null,
    cancelText: String? = null,
    title: String,
    message: String,
    onCancel: () -> Unit,
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    image: Int? = null,
    sticky:Boolean = false,
    onConfirmed: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = if(sticky) DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ) else DialogProperties()
    ) {
        (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(0.85f)
        Box() {

            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                if (image != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Image(
                            painter = painterResource(id = image),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Transparent, RoundedCornerShape(50.dp))
                        )
                    }
                }

                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W700),
                    textAlign = TextAlign.Center,
                    modifier = if (isAlert) Modifier.fillMaxWidth() else Modifier
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Left
                )

                Row(modifier = Modifier.padding(top = 20.dp)) {
                    if (!isAlert) {
                        Card(
                            modifier = Modifier
                                .height(40.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(90.dp))
                                .clickOnce {
                                    onCancel()
                                },
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(90.dp),
                            border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = cancelText ?: stringResource(R.string.no),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W200),
                                    textAlign = TextAlign.Center
                                )

                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                    }
                    Card(
                        modifier = Modifier
                            .weight(if (isAlert) 0.01f else 1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(90.dp))
                            .clickOnce {
                                onConfirmed()
                            },
                        elevation = CardDefaults.elevatedCardElevation(5.dp),
                        colors = CardDefaults.cardColors(confirmColor),
                        shape = RoundedCornerShape(90.dp),
                    ) {
                        Row(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = confirmText ?: stringResource(
                                    if (!isAlert) {
                                        R.string.yes
                                    } else {
                                        R.string.ok
                                    }
                                ),

                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W700),
                                textAlign = TextAlign.Center
                            )

                        }
                    }
                }
            }

            if (closeButton) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Button",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(top = 12.dp, end = 12.dp)
                        .size(30.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(90.dp)
                        )
                        .clip(RoundedCornerShape(90.dp))
                        .clickOnce {
                            onCancel()
                        }
                        .padding(5.dp)
                )
            }
        }
    }
}
