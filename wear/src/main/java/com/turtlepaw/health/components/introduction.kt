package com.turtlepaw.health.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi

@Composable
@OptIn(ExperimentalHorologistApi::class)
fun Introduction(
    appName: String,
    description: String,
    features: List<Triple<ImageVector, String, String>>,
    onButtonClick: () -> Unit,
) {
    Page {
        item {
            Text(
                text = appName,
                style = MaterialTheme.typography.title2
            )
        }
        item {
            Text(
                text = description,
                style = MaterialTheme.typography.body2
            )
        }
        items(features) {
            Card(
                enabled = false,
                onClick = {}
            ) {
                Icon(
                    imageVector = it.first,
                    contentDescription = it.second
                )
                Text(
                    it.second,
                    style = MaterialTheme.typography.title3
                )
                Text(
                    it.third,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}