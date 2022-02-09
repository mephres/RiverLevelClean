package com.intas.metrolog.ui.operation.operation_control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.FieldItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.dict_data.FieldDictData
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util
import kotlinx.coroutines.launch

class OperationControlInputValueViewModel(application: Application): AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private var _fieldList = MutableLiveData<List<FieldItem>>()
    val fieldList: LiveData<List<FieldItem>>
        get()  = _fieldList

    private val _dictDataMap = MutableLiveData<HashMap<Long, List<FieldDictData>>>()
    val dictDataMap: LiveData<HashMap<Long, List<FieldDictData>>>
        get()  = _dictDataMap

    /**
     * Получение списка параметров операционного контроля для данной операции мероприятия
     * @param operationId - идентификатор операции мероприятия
     */
    fun getOperationDoneParameter(operationId: Long) {

        db.fieldDao().getFieldsByOperationId(operationId)?.let {
            _fieldList.value = it
            getFieldDictData(it)
        }
    }

    /**
     * Получение списка методов измерения (списки для Spinner) для каждого элемента списка параметров операционного контроля
     * @param fieldList - список параметров операционного контроля операции мероприятия
     */
    private fun getFieldDictData(fieldList: List<FieldItem>) {
        val tempDictDataMap = HashMap<Long, List<FieldDictData>>()
        fieldList.forEach { field->
            if (field.type.equals("dict", true)) {
                db.fieldDictDataDao().getDictDataByFieldId(field.id)?.let {
                    tempDictDataMap.put(field.id, it)
                }
            }
        }
        _dictDataMap.value = tempDictDataMap
    }

    /**
     * Сохранение введенного значения параметра операционного контроля
     * @param field - параметр операционного контроля, объект типа [FieldItem]
     * @param value - введенное значение для параметра
     */
    fun saveParameter(field: FieldItem, value: String) {
        viewModelScope.launch {
            field.value = value
            field.userId = Util.authUser?.userId ?: 0
            field.dateTime = DateTimeUtil.getUnixDateTimeNow()
            field.isSended = 0

            db.fieldDao().updateField(field)
        }
    }

    /**
     * Установка флага для отсылки операционного контроля на сервер
     */
    fun setOperationControlReadyToSend(eventId: Long, operationId: Long) {
        viewModelScope.launch {
            db.operControlDao().setEventOperationControlReadyForSendBy(eventId, operationId)
        }
    }
}