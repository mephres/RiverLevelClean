package com.intas.metrolog.ui.requests.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.request.RequestItem
import kotlinx.coroutines.launch

class AddRequestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    val disciplines = db.disciplineDao().getAllDiscipline()
    val operations = db.eventOperationDao().getAllEventOperation()
    val category = db.eventCommentDao().getAllEventComment()
    val priority = db.equipInfoPriorityDao().getAllEquipInfoPriority()


    fun addRequest(requestItem: RequestItem) {
        viewModelScope.launch {
            db.requestDao().insertRequest(requestItem)
        }
    }
}