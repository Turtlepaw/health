package com.turtlepaw.health.apps.health.presentation.pages.coaching

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.shared.components.Page
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.CoachingProgram
import com.turtlepaw.shared.database.CoachingType
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun StartCoaching(database: AppDatabase) {
    var coachType = remember { mutableStateOf<CoachingType?>(null) }
    val coroutineScope = rememberCoroutineScope()
    Page {
        item {
            Text(
                text = "Start Coaching",
                color = MaterialTheme.colors.onSurfaceVariant,
            )
        }
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                enabled = true,
                checked = coachType.value == CoachingType.Sleep,
                label = {
                    Text("Sleep")
                },
                toggleControl = {
                    RadioButton(
                        selected = coachType.value == CoachingType.Sleep
                    )
                },
                onCheckedChange = {
                    if (it) coachType.value = CoachingType.Sleep
                },
                appIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = "Bedtime",
                    )
                }
            )
        }
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        coachType.value?.let {
                            database.coachingProgramDao().startProgram(
                                CoachingProgram(
                                    it.name,
                                    0,
                                    emptyMap()
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = "Flag",
                    )

                    Text(
                        text = "Start Coaching",
                    )
                }
            }
        }
    }
}
