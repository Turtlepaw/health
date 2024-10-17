package com.turtlepaw.shared.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.jvm.java

class SunlightViewModel(application: Application) : AndroidViewModel(application) {
    private val database =
        AppDatabase.getDatabase(application)
    private val _sunlightData = MutableStateFlow<Int>(0)
    val sunlightData: StateFlow<Int> get() = _sunlightData
    private val _isSunlightEnabled = MutableStateFlow<Boolean>(false)
    val isSunlightEnabled: StateFlow<Boolean> get() = _isSunlightEnabled

    init {
        viewModelScope.launch {
            database.sunlightDao().getLiveDay(LocalDate.now())
                .collect { data ->
                    _sunlightData.value = data?.value ?: 0
                }

            _isSunlightEnabled.value =
                database.serviceDao()
                    .getService(ServiceType.SUNLIGHT.serviceName)?.isEnabled == true
        }
    }
}

class SunlightViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SunlightViewModel::class.java)) {
            return SunlightViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
