package com.turtlepaw.health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.turtlepaw.shared.database.services.Service
import com.turtlepaw.shared.database.services.ServiceType
import com.turtlepaw.shared.theming.PrimaryServiceColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination<MainGraph>
@Composable
fun SettingsPage(navigator: DestinationsNavigator) {
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    val database = LocalDatabase.current
    val serviceSyncClient = LocalServiceSyncClient.current
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        services = database.serviceDao().getAllServices()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.popBackStack()
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
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            Text(
                text = "Apps",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            ServiceType.entries.map {
                val interactionSource = remember { MutableInteractionSource() }
                val isEnabled = services.find { service -> service.name == it }?.isEnabled == true
                ListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier.background(
                                PrimaryServiceColors[it]!!.primary,
                                CircleShape
                            )
                        ) {
                            Icon(
                                painter = painterResource(
                                    when (it) {
                                        ServiceType.Sunlight -> R.drawable.sunlight
                                        ServiceType.Sleep -> R.drawable.sleep
                                    }
                                ),
                                contentDescription = "App Icon",
                                modifier = Modifier
                                    .size(45.dp)
                                    .padding(8.dp),
                                tint = Color.Black.copy(0.9f)
                            )
                        }
                    },
                    headlineContent = { Text(it.name.toString()) },
                    trailingContent = {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = null,
                            interactionSource = interactionSource,
                        )
                    },
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(
                            onClick = {
                                coroutineScope.launch {
                                    serviceSyncClient.setService(
                                        it,
                                        !isEnabled
                                    )

                                    services = database.serviceDao().getAllServices()
                                }
                            },
                            interactionSource = interactionSource,
                            indication = ripple(),
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .padding(vertical = 14.dp)
                )
            }
        }
    }
}

@Composable
fun ColoredTheme(baseColor: Color, content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = baseColor,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
