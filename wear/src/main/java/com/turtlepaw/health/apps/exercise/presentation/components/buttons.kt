package com.turtlepaw.health.apps.exercise.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.turtlepaw.health.R

@Composable
fun StartButton(
    progress: Animatable<Float, AnimationVector1D>? = null,
    timeRemaining: Animatable<Float, AnimationVector1D>? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            if (!((progress?.value ?: 0f) > 0f))
                onClick()
        }
    ) {
        // Circular progress indicator
        if (progress != null) {
            CircularProgressIndicator(
                progress = progress.value,
                indicatorColor = Color.Black,
                trackColor = if (progress.value == 0f) Color.Transparent else Color.Black.copy(
                    alpha = 0.18f
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
            )
        }

        if (progress?.value == 0f || progress?.value == null) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play Arrow",
                modifier = Modifier.size(32.dp)
            )
        } else {
            Text(
                text = (timeRemaining?.value?.toInt()).toString(),
                style = MaterialTheme.typography.title2,
            )
        }
    }
}

@Composable
fun EndButton(
    isEnding: Boolean,
    onClick: () -> Unit
) {
    Button(
        enabled = !isEnding,
        onClick = {
            onClick()
        }
    ) {
        if (isEnding) {
            CircularProgressIndicator(
                indicatorColor = Color.Black,
                trackColor = Color.Black.copy(
                    alpha = 0.18f
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun PauseButton(
    onClick: () -> Unit
) {
    Button(
        onClick = {
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Rounded.Pause,
            contentDescription = "Pause",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun CompleteButton(
    onClick: () -> Unit
) {
    Button(
        onClick = {
            onClick()
        }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.check),
            contentDescription = "check",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    showBackground = true,
    backgroundColor = 0xff000000
)
@Composable
fun ButtonsPreview() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        StartButton {}
        EndButton(false) {}
        PauseButton {}
    }
}