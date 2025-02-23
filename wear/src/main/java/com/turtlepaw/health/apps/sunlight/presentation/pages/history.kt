package com.turtlepaw.health.apps.sunlight.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.shared.components.Material3Page
import com.turtlepaw.shared.database.sunlight.SunlightDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun History(
    history: List<SunlightDay>
) {
    Material3Page {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "History",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleSmall
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
                enabled = false,
                onClick = { /*TODO*/ },
                title = {
                    val formattedDate = if (item.timestamp == today) "Today"
                    else if (item.timestamp == today.minusDays(1)) "Yesterday"
                    else item.timestamp?.format(DateTimeFormatter.ofPattern("dd MMM"))

                    Text(
                        text = formattedDate.toString(),
                        //style = MaterialTheme.typography.title3
                    )
                }
            ) {
                Text(text = "${item.value} min", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HistoryPreview() {
    History(
        history = listOf(
            LocalDate.now() to 10,
            LocalDate.now().minusDays(1) to 20,
            LocalDate.now().minusDays(2) to 30
        ).map {
            SunlightDay(
                timestamp = it.first,
                value = it.second
            )
        }
    )
}