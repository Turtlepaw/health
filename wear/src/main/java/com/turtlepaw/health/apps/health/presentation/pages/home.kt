package com.turtlepaw.health.apps.health.presentation.pages

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.components.Page
import com.turtlepaw.health.database.AppDatabase

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome(database: AppDatabase) {
    Page {
        item {
            Text(
                text = "Health",
                //modifier = Modifier.padding(bottom = 2.dp),
                color = MaterialTheme.colors.onSurfaceVariant,
                //fontWeight = FontWeight.W400
            )
        }
    }
}
