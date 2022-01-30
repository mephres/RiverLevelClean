package com.intas.metrolog.ui.operation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.pojo.event.EventItem

class OperationViewModelFactory(
    private val eventId: Long,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OperationViewModel::class.java)) {
            return OperationViewModel(application, eventId) as T
        }
        throw RuntimeException("Unknown view model class $modelClass")
    }
}