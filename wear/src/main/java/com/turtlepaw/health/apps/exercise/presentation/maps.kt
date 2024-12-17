package com.turtlepaw.health.apps.exercise.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.AppCard
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme

@Composable
fun Map(coordinates: List<Pair<Double, Double>>) {
    val color = MaterialTheme.colors.primary
    val gridLineColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)

    if (coordinates.isNotEmpty()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .border(
                    width = Stroke.HairlineWidth.dp,
                    color = gridLineColor,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            // Clip the canvas to avoid overflow
            clipRect {
                // Draw grid background
                val step = 20.dp.toPx() // Grid spacing
                for (x in 0..size.width.toInt() step step.toInt()) {
                    drawLine(
                        color = gridLineColor,
                        start = androidx.compose.ui.geometry.Offset(x.toFloat(), 0f),
                        end = androidx.compose.ui.geometry.Offset(x.toFloat(), size.height)
                    )
                }
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawLine(
                        color = gridLineColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y.toFloat()),
                        end = androidx.compose.ui.geometry.Offset(size.width, y.toFloat())
                    )
                }

                // Normalize coordinates for Canvas dimensions
                val drawablePoints = normalizeCoordinates(
                    coordinates,
                    size.width,
                    size.height
                )

                // Draw the path
                val path = Path().apply {
                    if (drawablePoints.size > 1) {
                        val firstPoint = drawablePoints.first()
                        moveTo(firstPoint.first, firstPoint.second)

                        for (i in 1 until drawablePoints.size) {
                            val prevPoint = drawablePoints[i - 1]
                            val currPoint = drawablePoints[i]
                            val midPointX = (prevPoint.first + currPoint.first) / 2
                            val midPointY = (prevPoint.second + currPoint.second) / 2
                            quadraticTo(prevPoint.first, prevPoint.second, midPointX, midPointY)
                        }
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No data to display", style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
fun MapRoute(coordinates: List<Pair<Double, Double>>?) {
    AppCard(
        content = {
            Map(coordinates ?: emptyList())
        },
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = MaterialTheme.colors.surface,
            endBackgroundColor = MaterialTheme.colors.surface
        ),
        appImage = {
            Icon(
                imageVector = Icons.Rounded.Map,
                contentDescription = "Map Route",
                tint = MaterialTheme.colors.primary
            )
        },
        appName = {
            Text("Route")
        },
        title = {},
        enabled = false,
        time = {},
    )
}

fun normalizeCoordinates(
    coordinates: List<Pair<Double, Double>>,
    canvasWidth: Float,
    canvasHeight: Float,
    padding: Float = 15.dp.value // Add padding here
): List<Pair<Float, Float>> {
    if (coordinates.isEmpty()) return emptyList()

    // Extract latitudes and longitudes
    val latitudes = coordinates.map { it.first.toFloat() }
    val longitudes = coordinates.map { it.second.toFloat() }

    val minLat = latitudes.minOrNull() ?: 0f
    val maxLat = latitudes.maxOrNull() ?: 0f
    val minLon = longitudes.minOrNull() ?: 0f
    val maxLon = longitudes.maxOrNull() ?: 0f

    val latRange = (maxLat - minLat).takeIf { it > 0f } ?: 1f
    val lonRange = (maxLon - minLon).takeIf { it > 0f } ?: 1f

    // Adjust canvas size for padding
    val paddedWidth = canvasWidth - 2 * padding
    val paddedHeight = canvasHeight - 2 * padding

    // Scale factor to fit within padded area
    val scale = minOf(paddedWidth / lonRange, paddedHeight / latRange)

    // Center offsets to ensure the path is within the padding
    val offsetX = padding + (paddedWidth - lonRange * scale) / 2
    val offsetY = padding + (paddedHeight - latRange * scale) / 2

    // Normalize coordinates
    return coordinates.map { (lat, lon) ->
        val x = ((lon.toFloat() - minLon) * scale) + offsetX
        val y = canvasHeight - (((lat.toFloat() - minLat) * scale) + offsetY)
        Pair(x, y)
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFF000000
)
@Composable
fun MapRoutePreview() {
    // Extended sample coordinates with a more complex route
    val sampleCoordinates = listOf(
        Pair(37.7749, -122.4194), // Start
        Pair(37.7751, -122.4185),
        Pair(37.7754, -122.4175),
        Pair(37.7757, -122.4168),
        Pair(37.7760, -122.4162), // Curve down
        Pair(37.7762, -122.4165),
        Pair(37.7764, -122.4170),
        Pair(37.7766, -122.4176),
        Pair(37.7765, -122.4182), // Curve back up
        Pair(37.7763, -122.4187),
        Pair(37.7760, -122.4190),
        Pair(37.7757, -122.4193), // End with curve
        // Additional points for a larger path
        Pair(37.7750, -122.4198),
        Pair(37.7748, -122.4201),
        Pair(37.7745, -122.4205),
        Pair(37.7742, -122.4208),
        Pair(37.7740, -122.4210), // Diagonal down
        Pair(37.7737, -122.4207),
        Pair(37.7735, -122.4202),
        Pair(37.7733, -122.4198),
        Pair(37.7731, -122.4193),
        Pair(37.7730, -122.4188), // Loop back
        Pair(37.7732, -122.4183),
        Pair(37.7735, -122.4180),
        Pair(37.7738, -122.4178),
        Pair(37.7741, -122.4176),
        Pair(37.7744, -122.4175), // Connect back up
        Pair(37.7747, -122.4176),
        Pair(37.7750, -122.4178),
    )

    ExerciseTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(25.dp),
            contentAlignment = Alignment.Center
        ) {
            MapRoute(sampleCoordinates)
        }
    }
}

