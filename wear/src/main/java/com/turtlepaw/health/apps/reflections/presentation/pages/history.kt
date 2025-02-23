package com.turtlepaw.health.apps.reflections.presentation.pages

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.shared.components.Page
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.reflect.Reflection

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun History(database: AppDatabase) {
    val reflections = remember { mutableStateOf<List<Reflection>?>(null) }
    rememberCoroutineScope()
    LaunchedEffect(Unit) {
        reflections.value = database.reflectionDao().getHistory()
        Log.d("History", reflections.value.toString())
    }

    Page {
        if (reflections.value != null) {
            items(reflections.value as List<Reflection>) {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(it.value.displayName)
                    },
                    onClick = {},
                    secondaryLabel = {
                        Text(
                            getRelativeTime(it.date)
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                iconMappings[it.value] ?: R.drawable.sentiment_satisfied
                            ),
                            contentDescription = it.value.displayName
                        )
                    }
                )
            }
        } else {
            item {
                CircularProgressIndicator()
            }
        }
    }
}