package com.turtlepaw.health.apps.health.tile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ModifiersBuilders.Padding
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.turtlepaw.health.apps.sunlight.tile.MainTileService
import com.turtlepaw.shared.SettingsBasics
import com.turtlepaw.shared.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


private const val RESOURCES_VERSION = "1"
const val DEFAULT_GOAL = 8 // 8hrs
private const val LAUNCH_APP_ID = "LAUNCH_APP"
enum class Images(private val id: String) {
    STEPS("steps");

    fun getId(): String {
        return id
    }
}

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "MainTileService"
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                Images.STEPS.getId(),
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(com.turtlepaw.heart_connection.R.drawable.steps)
                        .build()
                    ).build()
            ).build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val lastClickableId = requestParams.currentState.lastClickableId
        if (lastClickableId == LAUNCH_APP_ID) {
            Log.d("Tile", "Launching main activity...")
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
        }

        getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        val database = AppDatabase.getDatabase(this)
        val stepStreak = database.dayDao().getDays().filter { it.steps >= it.goal }.size

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(
                    // https://stackoverflow.com/a/77947118/15751555
                    LayoutElementBuilders.Box.Builder()
                        .setWidth(expand())
                        .setHeight(expand())
                        .setModifiers(
                            Modifiers.Builder()
                                .setClickable(
                                    ModifiersBuilders.Clickable.Builder()
                                        .setId(LAUNCH_APP_ID)
                                        .setOnClick(
                                            ActionBuilders.LoadAction.Builder()
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .addContent(
                            if (stepStreak == 0) noDataLayout(
                                this,
                                requestParams.deviceConfiguration
                            ).build() else tileLayout(
                                this,
                                stepStreak,
                                requestParams.deviceConfiguration
                            ).build()
                        )
                        .build()
                ).build()
            ).build()
        ).build()


        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline)
            .setFreshnessIntervalMillis(
                // Every 5 minutes (60000 = 1m)
                60000 * 5
            )
            .build()
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        serviceScope.launch {
            try {
                getUpdater(this@MainTileService).requestUpdate(MainTileService::class.java)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to request tile update on enter", e)
            }
        }
    }
}

private fun noDataLayout(
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
): EdgeContentLayout.Builder {
    return EdgeContentLayout.Builder(deviceParameters)
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, "Step Streak")
                .setTypography(Typography.TYPOGRAPHY_TITLE3)
                .setColor(argb(TileColors.LightText))
                .build()
        )
        .setSecondaryLabelTextContent(
                    Text.Builder(
                        context,
                        "Reach your goal before midnight."
                    ).setTypography(Typography.TYPOGRAPHY_BODY2)
                        .setColor(argb(TileColors.White))
                        .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE_END)
                        .setMaxLines(2)
                        .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
                        .setModifiers(
                            Modifiers.Builder()
                                .setPadding(
                                    Padding.Builder()
                                        .setTop(
                                            dp(10f)
                                        )
                                        .build()
                                )
                                .build()
                )
//                .addContent(
//                    Text.Builder(context, sleepQuality.getTitle())
//                        .setTypography(Typography.TYPOGRAPHY_BODY2)
//                        .setColor(argb(TileColors.White))
//                        .build()
//                )
                .build()
        )
//        .setContent(
//            Spannable.Builder()
//                .addSpan(
//                    SpanText.Builder()
//                        .setText("No Data")
//                        .setFontStyle(
//                            FontStyle.PrimaryFontSize.getBuilder()
//                        )
//                        .build()
//                )
//                .build()
//        )
}

private fun tileLayout(
    context: Context,
    today: Int,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
): PrimaryLayout.Builder {
    DateTimeFormatter.ofPattern("h:mma")
    return PrimaryLayout.Builder(deviceParameters)
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, "Step Streak")
                .setTypography(Typography.TYPOGRAPHY_TITLE3)
                .setColor(argb(TileColors.LightText))
                .build()
        )
        .setSecondaryLabelTextContent(
            Text.Builder(context, "Keep going!")
                .setTypography(Typography.TYPOGRAPHY_BODY2)
                .setColor(argb(TileColors.LightText))
                .build()
        )
        .setContent(
            Text.Builder(context, "Day $today")
                .setTypography(Typography.TYPOGRAPHY_DISPLAY3)
                .setColor(argb(TileColors.PrimaryColor))
                .build()
        )
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.SMALL_ROUND, fontScale = 1.24f)
@Preview(device = WearDevices.LARGE_ROUND)
@Preview(device = WearDevices.LARGE_ROUND, fontScale = 1.24f)
fun TilePreview(context: Context) = TilePreviewData(
    onTileResourceRequest = {
        ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                Images.STEPS.getId(),
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(com.turtlepaw.heart_connection.R.drawable.steps)
                            .build()
                    ).build()
            ).build()
    }
) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        tileLayout(
            context,
            10,
            it.deviceConfiguration
        ).build()
    ).build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.SMALL_ROUND, fontScale = 1.24f)
@Preview(device = WearDevices.LARGE_ROUND)
@Preview(device = WearDevices.LARGE_ROUND, fontScale = 1.24f)
fun NoDataPreview(context: Context) = TilePreviewData(
    onTileResourceRequest = {
        ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                Images.STEPS.getId(),
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(com.turtlepaw.heart_connection.R.drawable.steps)
                            .build()
                    ).build()
            ).build()
    }
) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        noDataLayout(
            context,
            it.deviceConfiguration
        ).build()
    ).build()
}