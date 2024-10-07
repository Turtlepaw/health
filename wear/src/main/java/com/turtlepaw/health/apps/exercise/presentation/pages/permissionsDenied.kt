package com.turtlepaw.heartconnect.presentation.pages

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.components.Page
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun PermissionsDenied(
    onRetry: () -> Unit
) {
    ExerciseTheme {
        Page {
            item {
                Text(
                    text = "Some permissions have not been granted",
                    textAlign = TextAlign.Center
                )
            }
            item {
                Spacer(modifier = Modifier.padding(5.dp))
            }
            item {
                Button(
                    onClick = {
                        Log.d("PermissionsDenied", "Clicked")
                        onRetry()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Grant Permissions")
                }
            }
        }
    }
}