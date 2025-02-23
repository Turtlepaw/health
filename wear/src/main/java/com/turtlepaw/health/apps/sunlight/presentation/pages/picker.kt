package com.turtlepaw.health.apps.sunlight.presentation.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun StatePicker(
    options: List<Int>,
    unitOfMeasurement: String,
    currentState: Int,
    recommendedItem: Int? = null,
    renderItem: ((Int) -> String) = { it.toString() },
    onSelect: (Int) -> Unit
) {
    val initialIndex = options.indexOf(currentState)
    val state = rememberPickerState(
        initialNumberOfOptions = options.size,
        initiallySelectedIndex = if (initialIndex != -1) initialIndex else 0
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        ListHeader {
//            Image(
//                asset = CommunityMaterial.Icon3.cmd_timer_cog,
//                contentDescription = stringResource(R.string.refresh_interval),
//                colorFilter = ColorFilter.tint(LocalContentColor.current)
//            )
//        }
        Picker(
            state = state,
            contentDescription = "Goal",
            modifier = Modifier
                .weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${options[it]}$unitOfMeasurement",
                    style = with(LocalDensity.current) {
                        MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Medium,
                            // Ignore text scaling
                            fontSize = MaterialTheme.typography.displayLarge.fontSize.value.dp.toSp()
                        )
                    },
                    color = MaterialTheme.colorScheme.primary,
                    // In case of overflow, minimize weird layout behavior
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                if (it == recommendedItem) {
                    Text(
                        text = "(recommended)",
                        style = with(LocalDensity.current) {
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                // Ignore text scaling
                                fontSize = MaterialTheme.typography.labelSmall.fontSize.value.dp.toSp()
                            )
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // In case of overflow, minimize weird layout behavior
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                }
            }
        }
        Button(
            onClick = {
                onSelect(options[state.selectedOptionIndex])
            },
            colors = ButtonDefaults.filledTonalButtonColors(),
            modifier = Modifier
            //.wrapContentSize(align = Alignment.Center)
        ) {
            // Icon for history button
            Icon(
                painter = painterResource(id = R.drawable.check),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Settings",
                modifier = Modifier
                    .padding(2.dp)
            )
        }
//        FilledIconButton(
//            onClick = { onSelectInterval(options[state.selectedOption]) },
//            modifier = Modifier.touchTargetAwareSize(IconButtonDefaults.SmallButtonSize)
//        ) {
//            Icon(
//                Icons.Filled.Check,
//                contentDescription = stringResource(id = R.string.save),
//                modifier = Modifier.size(IconButtonDefaults.iconSizeFor(IconButtonDefaults.SmallButtonSize))
//            )
//        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun PreviewRefreshIntervalPickerView() {
    CompositionLocalProvider {
        StatePicker(
            currentState = 1,
            unitOfMeasurement = "m",
            options = List(60){
                it.plus(1)
            },
            recommendedItem = null
        ) {}
    }
}