package app.mindguru.android.ui.chat

import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import app.mindguru.android.R
import app.mindguru.android.data.model.User
import app.mindguru.android.ui.theme.IconTint
import app.mindguru.android.ui.theme.fontFamily
import coil.compose.AsyncImage
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.InfoPanelStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.TableStyle
import com.halilibo.richtext.ui.material3.Material3RichText
import com.valentinilk.shimmer.shimmer

private const val ARROW_PADDING = 16
private const val ARROW_WIDTH = 24
private const val ARROW_HEIGHT = 8
private const val COMPONENT_MIN_WIDTH = ARROW_WIDTH * 3

enum class BubbleArrowAlignment {
    LeftTop,
    BottomLeft,
    BottomRight
}

@Composable
fun chatBubbleShapeWithArrow(arrowAlignment: BubbleArrowAlignment = BubbleArrowAlignment.LeftTop): GenericShape {
    val density = LocalDensity.current
    val cornerRadius = 25F
    val triangleHeight = 20F
    val triangleWidth = 20F

    return GenericShape { size, _ ->
        val width = size.width
        val height = size.height
        with(density) {
            moveTo(cornerRadius, 0f)
            lineTo(width - cornerRadius, 0f)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    Offset(width - cornerRadius, 0f),
                    Size(cornerRadius, cornerRadius)
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(width, height - cornerRadius)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    Offset(width - cornerRadius, height - cornerRadius),
                    Size(cornerRadius, cornerRadius)
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(cornerRadius, height)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    Offset(0f, height - cornerRadius),
                    Size(cornerRadius, cornerRadius)
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(0f, triangleHeight + cornerRadius)
            lineTo(-triangleWidth, triangleHeight*1.5F)
            lineTo(0f, cornerRadius)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    Offset(0f, 0f),
                    Size(cornerRadius, cornerRadius)
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
    }
}
@Composable
fun ChatBubble(
    message: String,
    isUserMessage: Boolean,
    loading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (isUserMessage) {
            if (User.currentUser != null && User.currentUser!!.picture != "null") {
                AsyncImage(
                    model = User.currentUser!!.picture,
                    contentDescription = "User",
                    modifier = Modifier
                        .size(25.dp)
                        .background(Color.Transparent, CircleShape)
                        .clip(CircleShape)
                )
            }else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "App Icon",
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }else{
            Icon(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "App Icon",
                modifier = Modifier.size(25.dp),
                tint = IconTint
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface, //if (!isUserMessage) MaterialTheme.colorScheme.primary else
                    shape = chatBubbleShapeWithArrow()
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    shape = chatBubbleShapeWithArrow()
                )
                .padding(8.dp)
        ) {
            if(loading) {
                ShimmerLoading()
            }else if(isUserMessage) {
                Text(
                    // Replace between {{ }} with empty string
                    text = message.replace(Regex("\\[\\[.*?\\]\\]"), "").trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface //if (!isUserMessage) MaterialTheme.colorScheme.onPrimary else
                )
            }else{
                Material3RichText(
                    modifier = Modifier.padding(0.dp),
                    style = RichTextStyle(
                        codeBlockStyle = CodeBlockStyle(
                            textStyle = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 14.sp,
                                color = White
                            ),
                            wordWrap = true,
                            modifier = Modifier.background(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        ),
                        tableStyle = TableStyle(borderColor = MaterialTheme.colorScheme.onBackground),
                        infoPanelStyle = InfoPanelStyle(textStyle = { TextStyle(color = MaterialTheme.colorScheme.primary) })
                    )
                ) {
                    Markdown(content = message.trim())
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatBubblePreview() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ChatBubble(message = "Hello, how can I help you?", isUserMessage = false, true)
        ChatBubble(message = "Hello, how can I help you?", isUserMessage = false)
        ChatBubble(message = "I need some assistance with my order.I need some assistance with my order.I need some assistance with my order.I need some assistance with my order.I need some assistance with my order.I need some assistance with my order.I need some assistance with my order.I need some assistance with my order.I need some assistance with my order.", isUserMessage = true)
    }
}

@Composable
fun ShimmerLoading() {
    Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .shimmer()
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        RoundedCornerShape(5.dp)
                    )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .shimmer()
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        RoundedCornerShape(5.dp)
                    )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(15.dp)
                    .shimmer()
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        RoundedCornerShape(5.dp)
                    )
            )
        }
}