package com.turtlepaw.health

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.turtlepaw.shared.SUNLIGHT_DATA_PATH
import com.turtlepaw.shared.SUNLIGHT_GOAL_NAME
import com.turtlepaw.shared.SUNLIGHT_VALUE_NAME
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.SunlightDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate

class SyncService : WearableListenerService() {
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val dataClient by lazy { Wearable.getDataClient(this) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    override fun onCreate() {
        super.onCreate()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val sharedPrefs = SharedPrefs(this)
        dataEvents.forEach { event ->
            // DataItem changed
            if (event.type == DataEvent.TYPE_CHANGED) {
                event.dataItem.also { item ->
                    if (item.uri.path?.compareTo(SUNLIGHT_DATA_PATH) == 0) {
                        DataMapItem.fromDataItem(item).dataMap.apply {
                            updateData(getInt(SUNLIGHT_VALUE_NAME))
                            sharedPrefs.setSunlightGoal(getInt(SUNLIGHT_GOAL_NAME))
                        }
                    }
                }
            } else if (event.type == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }

        sharedPrefs.setLastSynced()
    }

    fun updateData(data: Int) {
        scope.launch {
            AppDatabase.getDatabase(this@SyncService).sunlightDao().insertDay(
                SunlightDay(
                    timestamp = LocalDate.now(),
                    value = data
                )
            )
            Log.d("SyncService", "Updated sunlight for today: $data")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}