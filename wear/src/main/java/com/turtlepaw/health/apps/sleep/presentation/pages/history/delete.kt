package com.turtlepaw.health.apps.sleep.presentation.pages.history

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.components.Page
import com.turtlepaw.health.database.AppDatabase
import com.turtlepaw.health.database.SleepDay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class Item {
    class LocalDateTimeItem(val value: LocalDateTime) : Item()
    class StringItem(val value: String) : Item()
}

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearHistoryDelete(
    database: AppDatabase,
    id: String,
    navigation: NavHostController,
    onDelete: (time: String) -> Unit
) {
    val dayFormatter = DateTimeFormatter.ofPattern("E d")
    val timeFormatter = DateTimeFormatter.ofPattern("E h:mm a")
    var history by remember { mutableStateOf<SleepDay?>(null) }
    var loading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit) {
        if (id != "ALL") {
            history = database.sleepDao().getDay(id)
        }
        loading = false
    }

    Page(
        showTimeText = false
    ) {
        if (loading) {
            item {
                CircularProgressIndicator()
            }
        } else {
            item {
                Text(
                    text = "Delete ${
                        when (id) {
                            "ALL" -> "All"
                            else -> {
                                dayFormatter.format(history?.bedtime)
                            }
                        }
                    }?",
                    style = MaterialTheme.typography.title3
                )
            }
            item {
                Text(
                    text = "${if (id != "ALL") timeFormatter.format(history?.bedtime) else "All of your recorded bedtimes"} will be permanently deleted",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(
                            top = 5.dp
                        )
                )
            }
            item {
                Spacer(modifier = Modifier.padding(3.dp))
            }
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            onDelete(id)
                            if (id == "ALL") database.sleepDao().deleteAll()
                            navigation.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error
                    )
                ) {
                    Text(
                        text = "Delete${if (id == "ALL") " All" else ""}",
                        color = Color.Black
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        navigation.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.surface,
                    )
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White
                    )
                }
            }
        }
    }
}