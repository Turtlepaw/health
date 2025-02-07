package com.turtlepaw.health.apps.sleep

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmActivity : ComponentActivity() {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator
    private var isPlaying = false

    private val alarmJob = Job()
    private val alarmScope = CoroutineScope(Dispatchers.Main + alarmJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindow()
        initializeHardware()
        startAlarm()

        setContent {
            AlarmScreen(
                onDismiss = { dismissAlarm() },
                onSnooze = { snoozeAlarm() }
            )
        }
    }

    private fun setupWindow() {
        // Ensure we can show even on lockscreen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Acquire wake lock
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SmartAlarm::AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L /*10 minutes*/)
        }
    }

    private fun initializeHardware() {
        // Initialize vibrator
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Initialize media player with alarm sound
        mediaPlayer = MediaPlayer().apply {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            setDataSource(applicationContext, alarmUri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            isLooping = true
            prepare()
        }
    }

    private fun startAlarm() {
        isPlaying = true

        // Start playing alarm
        mediaPlayer.start()

        // Start vibration pattern
        alarmScope.launch {
            val vibrationPattern =
                longArrayOf(0, 500, 500) // Pattern: wait 0ms, vibrate 500ms, sleep 500ms
            while (isPlaying) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        vibrationPattern,
                        0 // Repeat from index 0
                    )
                )
                delay(1500) // Wait for pattern to complete
            }
        }

        // Auto-dismiss after 10 minutes
        alarmScope.launch {
            delay(10 * 60 * 1000L)
            dismissAlarm()
        }
    }

    private fun dismissAlarm() {
        isPlaying = false
        stopAlarmAndCleanup()
        finish()
    }

    private fun snoozeAlarm() {
        // Schedule new alarm for 10 minutes later
        val intent = Intent(this, ModernSleepTrackerService::class.java).apply {
            action = "START"
            putExtra("snooze", true)
            putExtra("snoozeMinutes", 10)
        }
        startService(intent)

        dismissAlarm()
    }

    private fun stopAlarmAndCleanup() {
        // Stop sound and vibration
        mediaPlayer.stop()
        vibrator.cancel()

        // Release resources
        mediaPlayer.release()
        wakeLock.release()
        alarmJob.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlaying) {
            stopAlarmAndCleanup()
        }
    }
}

@Composable
fun AlarmScreen(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colors.primary.copy(.2f),
                        MaterialTheme.colors.primary.copy(.20f),
                        MaterialTheme.colors.primary.copy(.15f),
                        MaterialTheme.colors.primary.copy(.1f),
                        MaterialTheme.colors.primary.copy(.08f),
                        MaterialTheme.colors.primary.copy(.04f),
                        MaterialTheme.colors.primary.copy(.02f),
                        MaterialTheme.colors.background,
                    ),
                )
            )
            .blur(150.dp)
            .alpha(0.1f)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Alarm",
            style = MaterialTheme.typography.title2
        )

        Text(
            text = "It's time to wake up",
            style = MaterialTheme.typography.body1
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onSnooze,
                colors = ButtonDefaults.secondaryButtonColors()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Snooze,
                    contentDescription = "Snooze"
                )
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.primaryButtonColors()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
}

@WearPreviewSmallRound
@Composable
fun AlarmScreenPreview() {
    AlarmScreen(
        onDismiss = {},
        onSnooze = {}
    )
}