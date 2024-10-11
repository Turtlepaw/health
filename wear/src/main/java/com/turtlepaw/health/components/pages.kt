package com.turtlepaw.health.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ErrorPage(message: String, action: (() -> Unit)? = null, actionText: String? = "Retry") {
    Page {
        item {
            Icon(
                imageVector = Icons.Rounded.Error,
                contentDescription = "Error Icon",
                tint = MaterialTheme.colors.error
            )
        }
        item {
            Text(message, textAlign = TextAlign.Center)
        }
        if (action != null) {
            item {
                Button(onClick = action ?: {}) {
                    Text(actionText ?: "Retry")
                }
            }
        }
    }
}