package com.turtlepaw.health.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.shared.components.Material3Page

@Composable
@OptIn(ExperimentalHorologistApi::class)
fun Introduction(
    appName: String,
    description: String,
    features: List<Triple<ImageVector, String, String>>,
    onButtonClick: () -> Unit,
) {
    Material3Page {
        item {
            Text(
                text = appName,
                style = MaterialTheme.typography.titleSmall
            )
        }
        item {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(
                Modifier.height(1.dp)
            )
        }
        items(features) {
            Card(
                enabled = false,
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.Center) // Center the Column in the Box
                            .fillMaxSize() // Ensures Column takes up full size of Box
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp) // Optional: Add padding below Row
                        ) {
                            Icon(
                                imageVector = it.first,
                                contentDescription = it.second,
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                it.second,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            it.third,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        item {
            Button(
                onButtonClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Enable $appName"
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(2.dp))
        }
        item {
            Text(
                "$appName tracking may slightly decrease your battery life.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}