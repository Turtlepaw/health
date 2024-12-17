package com.turtlepaw.health.apps.sunlight.complication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.math.MathUtils
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.GoalProgressComplicationData
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.sunlight.presentation.MainActivity
import com.turtlepaw.shared.Settings
import com.turtlepaw.shared.SettingsBasics
import com.turtlepaw.shared.database.AppDatabase
import java.time.LocalDate


class MainComplicationService : SuspendingComplicationDataSourceService() {
    lateinit var appDatabase: AppDatabase
    private val supportedComplicationTypes = arrayOf(
        ComplicationType.SHORT_TEXT,
        ComplicationType.LONG_TEXT,
        ComplicationType.RANGED_VALUE
    )

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return createComplicationData(
            30,
            30,
            "30m",
            "Sunlight Today",
            type,
            this
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        appDatabase = AppDatabase.getDatabase(this)
        val today = appDatabase.sunlightDao().getDay(LocalDate.now())?.value ?: 0
        val goal = sharedPreferences.getInt(Settings.GOAL.getKey(), Settings.GOAL.getDefaultAsInt())
        Log.d("SunComplication", "Rendering complication as $today")
        return createComplicationData(
            today,
            goal,
            "${today}m",
            "Sunlight",
            request.complicationType,
            this
        )
    }

    private fun createComplicationData(
        sunlightToday: Int,
        goal: Int,
        text: String,
        contentDescription: String,
        type: ComplicationType,
        context: Context
    ): ComplicationData {
        Log.d("SunComplication", "Rendering complication...")
        val monochromaticImage = MonochromaticImage.Builder(
            Icon.createWithResource(context, R.drawable.sunlight)
        ).build()
        val smallImage = SmallImage.Builder(
            Icon.createWithResource(context, R.drawable.sunlight),
            SmallImageType.ICON
        ).build()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && type == ComplicationType.GOAL_PROGRESS){
            return GoalProgressComplicationData.Builder(
                value = sunlightToday.toFloat(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
                targetValue = goal.toFloat(),
            )
                .setText(
                    PlainComplicationText.Builder(text).build()
                )
                .setMonochromaticImage(monochromaticImage)
                .setTapAction(createActivityIntent(context))
                .setSmallImage(smallImage)
                .build()
        }

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text).build(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build()
            )
                .setMonochromaticImage(monochromaticImage)
                .setSmallImage(smallImage)
                .setTapAction(createActivityIntent(context))
                .build()
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text).build(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build()
            )
                .setMonochromaticImage(monochromaticImage)
                .setSmallImage(smallImage)
                .setTapAction(createActivityIntent(context))
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                min = 0f,
                max = goal.toFloat(),
                value = MathUtils.clamp(sunlightToday, 0, goal).toFloat(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            )
                .setText(
                    PlainComplicationText.Builder(text).build()
                )
                .setMonochromaticImage(monochromaticImage)
                .setTapAction(createActivityIntent(context))
                .setSmallImage(smallImage)
                .build()
            ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                monochromaticImage,
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            )
                .setTapAction(createActivityIntent(context))
                .build()
            ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
                smallImage,
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            )
                .setTapAction(createActivityIntent(context))
                .build()
            // Return default data
            else -> createComplicationData(
                sunlightToday,
                goal,
                text,
                contentDescription,
                ComplicationType.RANGED_VALUE,
                context
            )
        //throw IllegalArgumentException("unknown complication type")
        }
    }

    private fun createActivityIntent(context: Context): PendingIntent {
        // Create an Intent for the specific activity you want to launch
        val intent = Intent(context, MainActivity::class.java).apply {
            // Optionally add extras or flags to the intent
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        Log.d("ComplicationService", "Creating PendingIntent for MainActivity")

        return PendingIntent.getActivity(
            context,
            0, // Request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}