package com.turtlepaw.health.apps.sunlight.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.sunlight.presentation.theme.SunlightTheme
import com.turtlepaw.health.components.ItemsListWithModifier
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun Stats(
    history: Set<Pair<LocalDate, Int>?>
) {
    SunlightTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            TimeText(
                modifier = Modifier.scrollAway(scalingLazyListState)
            )
            PositionIndicator(
                scalingLazyListState = scalingLazyListState
            )
            ItemsListWithModifier(
                modifier = Modifier
                    .rotaryWithScroll(
                        reverseDirection = false,
                        focusRequester = focusRequester,
                        scrollableState = scalingLazyListState,
                    ),
                scrollableState = scalingLazyListState,
                verticalAlignment = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.Top,
                )
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bar_chart),
                            contentDescription = "Bar Chart",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(25.dp)
                        )

                        Text(
                            text = "Stats",
                            style = MaterialTheme.typography.title2
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(2.dp))
                }

                // Get the current date
                val today = LocalDate.now()

                item {
                    Card(
                        onClick = { /*TODO*/ },
                        backgroundPainter = CardDefaults.cardBackgroundPainter(
                            startBackgroundColor = MaterialTheme.colors.surface
                        ),
                    ) {
// Calculate the start of the current week (assuming week starts on Monday)
                        val startOfWeek: LocalDate = today.with(DayOfWeek.MONDAY)

// Calculate the end of the current week (assuming week ends on Sunday)
                        val endOfWeek: LocalDate = today.with(DayOfWeek.SUNDAY)

// Filter the history list to include only the items from this week and sum the second elements
                        val sumThisWeek = history.filterNotNull()
                            .filter { it.first in startOfWeek..endOfWeek }
                            .sumOf { it.second }

                        Text(
                            text = "Weekly",
                            style = MaterialTheme.typography.title3
                        )
                        Text(text = "${sumThisWeek} min", fontWeight = FontWeight.Medium)
                    }
                }

                item {
                    Card(
                        onClick = { /*TODO*/ },
                        backgroundPainter = CardDefaults.cardBackgroundPainter(
                            startBackgroundColor = MaterialTheme.colors.surface
                        ),
                    ) {
                        // Calculate the start of the current month
                        val startOfMonth: LocalDate =
                            today.with(TemporalAdjusters.firstDayOfMonth())

// Calculate the end of the current month
                        val endOfMonth: LocalDate = today.with(TemporalAdjusters.lastDayOfMonth())

// Filter the history list to include only the items from this month and sum the second elements
                        val sumThisMonth = history
                            .filterNotNull()
                            .filter { it.first in startOfMonth..endOfMonth }
                            .sumOf { it.second }

                        Text(
                            text = "Monthly",
                            style = MaterialTheme.typography.title3
                        )
                        Text(text = "${sumThisMonth} min", fontWeight = FontWeight.Medium)
                    }
                }

                item {
                    Card(
                        onClick = { /*TODO*/ },
                        backgroundPainter = CardDefaults.cardBackgroundPainter(
                            startBackgroundColor = MaterialTheme.colors.surface
                        ),
                    ) {
                        val sumThisYear = history.filterNotNull()
                            .filter { it.first.year == today.year }
                            .sumOf { it.second }

                        Text(
                            text = "Yearly",
                            style = MaterialTheme.typography.title3
                        )
                        Text(text = "${sumThisYear} min", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun StatsPreview() {
    Stats(
        history = emptySet()
    )
}