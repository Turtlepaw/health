package com.turtlepaw.health.apps.sunlight.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.shared.components.Material3Page
import com.turtlepaw.shared.database.sunlight.SunlightDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun Stats(
    history: List<SunlightDay>
) {
    Material3Page {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bar_chart),
                    contentDescription = "Bar Chart",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(25.dp)
                )

                Text(
                    text = "Stats",
                    style = MaterialTheme.typography.titleMedium
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
                enabled = false
            ) {
// Calculate the start of the current week (assuming week starts on Monday)
                val startOfWeek: LocalDate = today.with(DayOfWeek.MONDAY)

// Calculate the end of the current week (assuming week ends on Sunday)
                val endOfWeek: LocalDate = today.with(DayOfWeek.SUNDAY)

// Filter the history list to include only the items from this week and sum the second elements
                val sumThisWeek = history.filterNotNull()
                    .filter { it.timestamp in startOfWeek..endOfWeek }
                    .sumOf { it.value }

                Text(
                    text = "Weekly",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = "$sumThisWeek min", fontWeight = FontWeight.Medium)
            }
        }

        item {
            Card(
                onClick = { /*TODO*/ },
                enabled = false
            ) {
                // Calculate the start of the current month
                val startOfMonth: LocalDate =
                    today.with(TemporalAdjusters.firstDayOfMonth())

// Calculate the end of the current month
                val endOfMonth: LocalDate = today.with(TemporalAdjusters.lastDayOfMonth())

// Filter the history list to include only the items from this month and sum the second elements
                val sumThisMonth = history
                    .filterNotNull()
                    .filter { it.timestamp in startOfMonth..endOfMonth }
                    .sumOf { it.value }

                Text(
                    text = "Monthly",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = "$sumThisMonth min", fontWeight = FontWeight.Medium)
            }
        }

        item {
            Card(
                onClick = { /*TODO*/ },
                enabled = false
            ) {
                val sumThisYear = history.filterNotNull()
                    .filter { it.timestamp.year == today.year }
                    .sumOf { it.value }

                Text(
                    text = "Yearly",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = "$sumThisYear min", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun StatsPreview() {
    Stats(
        history = emptyList()
    )
}