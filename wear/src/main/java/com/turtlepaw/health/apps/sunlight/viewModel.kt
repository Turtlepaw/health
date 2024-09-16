package com.turtlepaw.health.apps.sunlight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.turtlepaw.health.database.AppDatabase
import com.turtlepaw.health.database.SunlightDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class SunlightViewModel(application: Application) : AndroidViewModel(application) {
    private val sunlightDao: SunlightDao = AppDatabase.getDatabase(application).sunlightDao()
    private val _sunlightData = MutableStateFlow<Int>(0)
    val sunlightData: StateFlow<Int> get() = _sunlightData

    init {
        viewModelScope.launch {
            sunlightDao.getLiveDay(LocalDate.now())
                .collect { data ->
                    _sunlightData.value = data?.value ?: 0
                }
        }
    }
}
