package com.turtlepaw.heartconnect.presentation.pages.history

import android.graphics.Typeface
import android.text.TextUtils
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.turtlepaw.health.components.Page
import com.turtlepaw.heartconnect.presentation.pages.MAX_HEARING
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHistory(
    history: List<Pair<LocalDateTime, Double>>,
    loading: Boolean,
    maxHearing: Int
) {
    ExerciseTheme {
        DateTimeFormatter.ofPattern("E d")
        DateTimeFormatter.ofPattern("h:mm a")
        val goal = MAX_HEARING

        Page {
            if (loading) {
                item {
                    CircularProgressIndicator()
                }
            } else if (history.isEmpty()) {
                item {
                    Text(text = "No history")
                }
            } else {
                SimpleDateFormat("hha", Locale.getDefault())
//                    val bottomAxisValueFormatter =  AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, axis ->
//                        val timeInMillis = value.toLong()
//                        val formattedTime = timeFormat.format(Date(timeInMillis))
//                        formattedTime
//                    }
                val bottomAxisValueFormatter =
                    AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, axis ->
                        return@AxisValueFormatter ""
//                            if (value == 0f) {
//                                // Display the time of the first record
//                                val firstRecordTime =
//                                    history.firstOrNull()?.first ?: LocalDateTime.now()
//                                return@AxisValueFormatter timeFormat.format(firstRecordTime)
//                            } else if (value == 6f) {
//                                // Display the time of the last record
//                                val lastRecordTime =
//                                    history.lastOrNull()?.first ?: LocalDateTime.now()
//                                return@AxisValueFormatter timeFormat.format(lastRecordTime)
//                            } else {
//                                // For all other values, return an empty string
//                                return@AxisValueFormatter ""
//                            }
                    }
                val currentDate = LocalDate.now()
                val today = history.filterNotNull().filter { date ->
                    date.first.toLocalDate() == currentDate
                }

                val rawData = List(7) { index ->
                    val date = today.elementAtOrNull(index)
                    if (date != null) {
                        Pair(
                            false,
                            entryOf(index.toFloat(), date.second.toFloat())
                        )
                    } else {
                        Pair(
                            true,
                            entryOf(index.toFloat(), 0f)
                        )
                    }
                }

                val data = rawData.map { data -> data.second }

                val chartEntryModelProducer = ChartEntryModelProducer(
                    data
                )

                item {
                    Text(text = "Today")
                }
                item {
                    Spacer(modifier = Modifier.padding(3.dp))
                }
                item {
                    Chart(
                        chart = columnChart(
                            axisValuesOverrider = AxisValuesOverrider.fixed(
                                maxY = goal.toFloat()
                            ),
                            spacing = 2.dp,
                            columns = today.map { (_, noise) ->
                                LineComponent(
                                    thicknessDp = 5f,
                                    shape = Shapes.roundedCornerShape(allPercent = 40),
                                    color = if (noise >= maxHearing) MaterialTheme.colors.primary.toArgb() else MaterialTheme.colors.secondary.toArgb(),
                                )
                            }
                        ),
                        chartModelProducer = chartEntryModelProducer,
                        startAxis = rememberStartAxis(
                            label = textComponent {
                                this.color = MaterialTheme.colors.onBackground.toArgb()
                                this.typeface = Typeface.MONOSPACE
                            },
                            guideline = LineComponent(
                                thicknessDp = 0.5f,
                                color = MaterialTheme.colors.surface.toArgb(),
                            ),
                        ),
                        bottomAxis = rememberBottomAxis(
                            label = textComponent {
                                this.color = MaterialTheme.colors.onBackground.toArgb()
                                this.ellipsize = TextUtils.TruncateAt.MARQUEE
                            },
                            valueFormatter = bottomAxisValueFormatter,
                            axis = LineComponent(
                                color = MaterialTheme.colors.surface.toArgb(),
                                thicknessDp = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .height(100.dp)
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(3.dp))
                }
                item {
                    Text(
                        text = "This graph shows how much noise you've been exposed to today",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun getRandomTime(amount: Int): List<Pair<LocalDateTime, Double>> {
    val randomTimes = mutableListOf<Pair<LocalDateTime, Double>>()

    repeat(amount) {
        val randomTime = LocalDateTime.now()
        randomTimes.add(
            Pair(
                randomTime,
                10.0
            )
        )
    }

    return randomTimes.toList()
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HistoryPreview() {
    WearHistory(
        history = getRandomTime(5),
        loading = false,
        maxHearing = MAX_HEARING.toInt()
    )
}