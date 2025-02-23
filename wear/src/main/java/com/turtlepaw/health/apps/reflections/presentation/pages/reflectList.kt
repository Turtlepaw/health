import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.reflections.presentation.pages.iconMappings
import com.turtlepaw.shared.database.reflect.ReflectionType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ReflectAsCircularIcons(context: Context, onConfirm: (reflection: ReflectionType) -> Unit) {
    var selectedReflection by remember { mutableStateOf(ReflectionType.Content) }
    val reflectionTypes = ReflectionType.entries.toTypedArray() // List of all reflections

    val density = LocalDensity.current // Get screen density for proper conversions
    val radius = 80.dp // Radius for icon placement

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp), // Full screen
        contentAlignment = Alignment.Center // Center everything inside
    ) {
        reflectionTypes.forEachIndexed { index, reflectionType ->
            // Calculate angle for each icon in circular layout
            val angle = (360f / reflectionTypes.size) * index

            // Convert radius from Dp to Px
            val radiusPx = with(density) { radius.toPx() - 25 }

            // Calculate x and y offsets for positioning the icons
            val xOffset = (radiusPx * cos(Math.toRadians(angle.toDouble()))).toFloat()
            val yOffset = (radiusPx * sin(Math.toRadians(angle.toDouble()))).toFloat()

            // Animate the icon size if it's the selected one
            val iconSize by animateDpAsState(
                targetValue = if (reflectionType == selectedReflection) 60.dp else 40.dp,
                animationSpec = tween(durationMillis = 100)
            )

            Icon(
                painter = painterResource(
                    iconMappings[reflectionType] ?: R.drawable.sentiment_neutral
                ),
                contentDescription = reflectionType.displayName,
                tint = if (reflectionType == selectedReflection) MaterialTheme.colors.primary else Color.Unspecified,
                modifier = Modifier
                    .size(iconSize) // Apply animated size
                    .layout { measurable, constraints ->
                        // Measure the icon and position it based on the calculated offsets
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(
                                x = (constraints.maxWidth / 2 - placeable.width / 2 + xOffset).toInt(),
                                y = (constraints.maxHeight / 2 - placeable.height / 2 + yOffset).toInt()
                            )
                        }
                    }
                    .clip(CircleShape)
                    .clickable {
                        selectedReflection = reflectionType
                        val vibrator = getSystemService(context, Vibrator::class.java)
                        vibrator?.let {
                            if (Build.VERSION.SDK_INT >= 26) {
                                it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                            } else {
                                @Suppress("DEPRECATION")
                                it.vibrate(100)
                            }
                        }
                    } // Change the selected mood on click
            )
        }

        // Confirmation Button in the center of the screen
        Button(
            onClick = { onConfirm(selectedReflection) },
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Confirm",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}