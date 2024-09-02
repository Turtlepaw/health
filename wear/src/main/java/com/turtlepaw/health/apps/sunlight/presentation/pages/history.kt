package com.turtlepaw.health.apps.sunlight.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TitleCard
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.sunlight.presentation.theme.SunlightTheme
import com.turtlepaw.health.components.ItemsListWithModifier
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun History(
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
                    )
                    .padding(bottom = 10.dp),
                scrollableState = scalingLazyListState,
                verticalAlignment = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.Top,
                ),
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.history),
                            contentDescription = "History",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "History",
                            style = MaterialTheme.typography.title3
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(2.dp))
                }

                // Get the current date
                val today = LocalDate.now()

                items(history.size) {
                    val item = history.elementAt(it)

                    TitleCard(
                        onClick = { /*TODO*/ },
                        backgroundPainter = CardDefaults.cardBackgroundPainter(
                            startBackgroundColor = MaterialTheme.colors.surface
                        ),
                        title = {
                            val formattedDate = if (item?.first == today) "Today"
                            else if (item?.first == today.minusDays(1)) "Yesterday"
                            else item?.first?.format(DateTimeFormatter.ofPattern("dd MMM"))

                            Text(
                                text = formattedDate.toString(),
                                //style = MaterialTheme.typography.title3
                            )
                        }
                    ) {
                        Text(text = "${item?.second} min", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HistoryPreview() {
    History(
        history = setOf(
            LocalDate.now() to 10,
            LocalDate.now().minusDays(1) to 20,
            LocalDate.now().minusDays(2) to 30
        )
    )
}