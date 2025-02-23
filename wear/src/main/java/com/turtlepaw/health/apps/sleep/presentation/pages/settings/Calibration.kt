package com.turtlepaw.health.apps.sleep.presentation.pages.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.MaterialTheme
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.sleep.SleepCalibration
import com.turtlepaw.shared.components.Material3Page
import com.turtlepaw.shared.database.AppDatabase

@Composable
fun Calibration() {
    var calibrationState by remember { mutableStateOf(SleepCalibration.CalibrationState.NOT_STARTED) }
    var dayCount by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        calibrationState = SleepCalibration(context).calibrationState
        AppDatabase.getDatabase(context).sleepDao().getAllSessions().collect {
            dayCount = it.size
        }
    }
    Material3Page {
        item {
            Box(
                modifier = Modifier.size(70.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.circle),
                    contentDescription = "Circle",
                    tint = if (calibrationState == SleepCalibration.CalibrationState.COMPLETED)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceContainer,
                )

                Icon(
                    painterResource(R.drawable.ic_vital_signs),
                    contentDescription = "Vital signs",
                    tint = if (calibrationState == SleepCalibration.CalibrationState.COMPLETED)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                )
            }
        }
        item {
            Spacer(
                modifier = Modifier.size(12.dp)
            )
        }
        item {
            Text(
                text = "Calibration " + when (calibrationState) {
                    SleepCalibration.CalibrationState.NOT_STARTED -> "Not started"
                    SleepCalibration.CalibrationState.IN_PROGRESS -> "In progress"
                    SleepCalibration.CalibrationState.COMPLETED -> "Completed"
                },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = when (calibrationState) {
                    SleepCalibration.CalibrationState.NOT_STARTED -> "Start tracking sleep to calculate your motion and heart rate baselines."
                    SleepCalibration.CalibrationState.IN_PROGRESS -> "We're still calculating your motion and heart rate baselines.\n\n${dayCount} of 3 days completed."
                    SleepCalibration.CalibrationState.COMPLETED -> "Your baselines for motion and heart rate for sleep tracking have been calculated.\n\nSleep tracking can now track your sleep more accurately and will continue to automatically adjust thresholds.\n\n"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalibrationAnimation(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    // Primary rotation for the outer ring
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Shape morphing animation
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Scale breathing animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Dot rotation animation

    Canvas(
        modifier = modifier
            .size(160.dp)
            .padding(12.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.5f

        // Draw outer ring with material shape transformation
        withTransform({
            rotate(rotationAngle, center)
            scale(scale, scale, center)
        }) {
            // Draw main circular track
            drawCircle(
                color = colors.surfaceContainerHigh.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw progress arc with material shape
            val sweepAngle = 120f
            drawArc(
                color = colors.primary,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.cornerPathEffect(8.dp.toPx())
                )
            )
        }

        // Draw inner material shape
        val innerRadius = radius * 0.4f
        withTransform({
            rotate(-rotationAngle * 0.5f, center)
            scale(1f + morphProgress * 0.15f, 1f + morphProgress * 0.15f, center)
        }) {
            // Inner circle with dynamic shape
            drawCircle(
                color = colors.tertiary.copy(alpha = 0.2f),
                radius = innerRadius,
                center = center,
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(8f, 8f),
                        phase = rotationAngle
                    )
                )
            )
        }

        // Central material point
        drawCircle(
            color = colors.primary,
            radius = 6.dp.toPx() * scale,
            center = center,
            style = Fill
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalibrationAnimationPreview() {
    MaterialTheme {
        CalibrationAnimation()
    }
}