package com.turtlepaw.health

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.turtlepaw.shared.WearableSettingsSyncViewModel
import com.turtlepaw.shared.database.ServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Destination<MainGraph>
@Composable
fun SettingsPage(navigator: DestinationsNavigator) {
    val viewModel = viewModel<WearableSettingsSyncViewModel>()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Arrow Back",
                        )
                    }
                }
            )
        }
    ) { padding ->
        val isServiceReady by viewModel.isServiceReady.collectAsState()
        if (!isServiceReady) {
            return@Scaffold Text("Loading...")
        }
        val sunlightEnabled by viewModel.wearableSettingsSyncService.getState<Boolean>(ServiceType.SUNLIGHT.serviceName)
            .collectAsState()
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            Text(
                text = "Apps",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            ListItem(
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.sunlight),
                        contentDescription = "Sunlight",
                        modifier = Modifier.size(48.dp)
                    )
                },
                headlineContent = { Text("Sunlight") },
                supportingContent = {
                    Text("Track your sunlight throughout the day")
                },
                trailingContent = {
                    Switch(
                        checked = sunlightEnabled == true,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(
                        onClick = {
                            viewModel.wearableSettingsSyncService.putService(
                                ServiceType.SUNLIGHT,
                                sunlightEnabled != true
                            )
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .padding(vertical = 8.dp)
            )
        }
    }
}