package app.mindguru.android.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.mindguru.android.ui.components.ToolBar
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
/*import com.himanshoe.charty.common.ChartDataCollection
import com.himanshoe.charty.common.config.AxisConfig
import com.himanshoe.charty.common.config.ChartDefaults
import com.himanshoe.charty.line.CurveLineChart
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartColors
import com.himanshoe.charty.line.config.LineChartDefaults
import com.himanshoe.charty.line.config.LineConfig
import com.himanshoe.charty.line.model.LineData*/
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoodTrackerScreen(
    navigateBack: () -> Unit = {},
    viewModel: MoodTrackerViewModel = hiltViewModel()) {
    val context  = LocalContext.current
    val moodLogs by viewModel.moodLogs.collectAsState(initial = emptyList())
    val loading by viewModel.loading.collectAsState(initial = false)
    val aiAnalysisResult by viewModel.aiAnalysisResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMoodLogs()
    }

    Column(Modifier.fillMaxSize()) {
        ToolBar(title = "Mood Tracker", navigateBack = navigateBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if(loading) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
                return
            }
            val pointsData: List<Point> = moodLogs.mapIndexed { index, moodLog ->
                Point(index.toFloat(), moodLog.mood.ordinal.toFloat())//moodLog.date.time
            }
            if (pointsData.isNotEmpty()) {
                val steps = 2F
                val xAxisData = AxisData.Builder()
                    .axisStepSize(100.dp)
                    //.backgroundColor(Color.Blue)
                    .steps(pointsData.size)
                    .labelData { i -> if( i == 0 || i == moodLogs.size-1  || i == moodLogs.size/2-1 ) getDate(moodLogs[i].date.time) else "" }
                    .labelAndAxisLinePadding(0.dp)
                    .build()

                val yAxisData = AxisData.Builder()
                    .steps(steps.toInt())
                    //.backgroundColor(Color.Red)
                    .labelAndAxisLinePadding(0.dp)
                    .labelData { i ->
                        when (i) {
                            0 -> "SAD"
                            1 -> "NEUTRAL"
                            2 -> "HAPPY"
                            else -> ""
                        }
                    }.build()
                val lineChartData = LineChartData(
                    linePlotData = LinePlotData(
                        lines = listOf(
                            Line(
                                dataPoints = pointsData,
                                LineStyle(),
                                null,
                                SelectionHighlightPoint(),
                                ShadowUnderLine(),
                                SelectionHighlightPopUp()
                            )
                        ),
                    ),
                    xAxisData = xAxisData,
                    yAxisData = yAxisData,
                    gridLines = null,
                    backgroundColor = Color.White
                )
                LineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    lineChartData = lineChartData
                )

                /*
                if (chartDataCollection.data.isNotEmpty()) {
                val chartDataCollection = ChartDataCollection(
                    data = moodLogs.value.map { moodLog ->
                        LineData(xValue = getDate(moodLog.date.time), yValue = moodLog.mood.ordinal.toFloat())
                    }
                )
                CurveLineChart(
                    dataCollection = chartDataCollection,
                    modifier = Modifier.height(200.dp),
                    padding = 16.dp,
                    axisConfig =  AxisConfig(
                        showAxes = true,
                        axisColor = Color.Gray,
                        axisStroke = 2f,
                        minLabelCount = 5,
                        showGridLabel = true,
                        showGridLines = false,
                    ),
                    radiusScale = 0.02f,
                    lineConfig =  LineConfig(
                        hasDotMarker = false,
                        strokeSize = 5f,
                        hasSmoothCurve = true,
                    )
                )*/
               /* Row(Modifier.fillMaxWidth()) {
                   Text(text = "Chart Specifications: X-Axis shows Date, Y-Axis shows Mood with values 0 is SAD, 1 is NEUTRAL, 2 is HAPPY", style = MaterialTheme.typography.bodyLarge,
                       textAlign = TextAlign.Center)
                }*/
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AI Analysis:",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                if(aiAnalysisResult.isNotEmpty()) {
                    Text(
                        text = aiAnalysisResult,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }else{
                    CircularProgressIndicator()
                }
            }
        }
    }
}

fun getDate(time: Long): String {
    val date = java.util.Date(time)
    val format = SimpleDateFormat("dd/MM", Locale.getDefault())///yyyy
    return format.format(date)
}