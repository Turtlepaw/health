package com.turtlepaw.health.apps.sleep.presentation.pages.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.sleep.utils.BedtimeViewModel
import com.turtlepaw.health.components.Page
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
    bedtimeViewModel: BedtimeViewModel,
    item: Item,
    navigation: NavHostController,
    onDelete: (time: Item) -> Unit
) {
        val dayFormatter = DateTimeFormatter.ofPattern("E d")
        val timeFormatter = DateTimeFormatter.ofPattern("E h:mm a")
        var history by remember { mutableStateOf<LocalDateTime?>(null) }
        var loading by remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(key1 = Unit) {
            when (item) {
                is Item.LocalDateTimeItem -> {
                    history = bedtimeViewModel.getItem(item.value.toString())
                }
                is Item.StringItem -> {
                    // do something with item.value as a String
                }
            }
            loading = false
        }

        Page(
            showTimeText = false
        ) {
            if(loading){
                item {
                    CircularProgressIndicator()
                }
            } else {
                item {
                    Text(
                        text = "Delete ${
                            when (item) {
                                is Item.LocalDateTimeItem -> {
                                    dayFormatter.format(item.value)
                                }

                                is Item.StringItem -> {
                                    "All"
                                }
                            }
                        }?",
                        style = MaterialTheme.typography.title3
                    )
                }
                item {
                    Text(
                        text = "${if (item is Item.LocalDateTimeItem) timeFormatter.format(item.value) else "All of your recorded bedtimes"} will be permanently deleted",
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
                @Suppress("KotlinConstantConditions")
                if (
                //item is Item.StringItem
                    "true" == "true"
                ) {
                    item {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    onDelete(item)
                                    bedtimeViewModel.deleteAll()
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
                                text = "Delete${if (item is Item.StringItem) " All" else ""}",
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
                } else {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                20.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            Button(
                                onClick = {
                                    navigation.popBackStack()
                                },
                                colors = ButtonDefaults.secondaryButtonColors(),
                                modifier = Modifier
                                    .size(ButtonDefaults.DefaultButtonSize)
                                //.wrapContentSize(align = Alignment.Center)
                            ) {
                                // Icon for history button
                                Icon(
                                    imageVector = Icons.Rounded.Cancel,
                                    contentDescription = "Cancel",
                                    tint = Color(0xFFE4C6FF),
                                    modifier = Modifier
                                        .padding(2.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        onDelete(
                                            item
                                        )

                                        if (item is Item.LocalDateTimeItem) {
                                            bedtimeViewModel.delete(item.value)
                                        } else {
                                            bedtimeViewModel.deleteAll()
                                        }

                                        navigation.popBackStack()
                                    }
                                },
                                colors = ButtonDefaults.secondaryButtonColors(),
                                modifier = Modifier
                                    .size(ButtonDefaults.DefaultButtonSize)
                                //.wrapContentSize(align = Alignment.Center)
                            ) {
                                // Icon for history button
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFE4C6FF),
                                    modifier = Modifier
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
    }
}