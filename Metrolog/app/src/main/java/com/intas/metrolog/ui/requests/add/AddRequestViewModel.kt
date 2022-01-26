package com.intas.metrolog.ui.requests.add

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.pojo.request.RequestPhoto
import com.intas.metrolog.util.getEncodedScreen
import kotlinx.coroutines.launch

class AddRequestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    val disciplines = db.disciplineDao().getAllDiscipline()
    val operationTypes = db.eventOperationTypeDao().getAllEventOperationType()
    val category = db.eventCommentDao().getAllEventComment()
    val priority = db.equipInfoPriorityDao().getAllEquipInfoPriority()

    var onRequestSavedSuccess: ((String) -> Unit)? = null
    var onEquipInfoSavedSuccess: ((Long) -> Unit)? = null

    private var _uriList = MutableLiveData<List<Uri>>()
    val uriList: LiveData<List<Uri>>
        get() = _uriList

    /**
     * Добавление в БД новой заявки и фото
     * @param requestItem - экземпляр новой заявки [RequestItem]
     */
    fun addRequest(requestItem: RequestItem) {
        viewModelScope.launch {
            val requestId = db.requestDao().insertRequest(requestItem)
            val imageList = uriList.value

            imageList?.forEach { uri ->
                    val requestPhoto = RequestPhoto(
                        requestId = requestId,
                        photo = getEncodedScreen(uri, getApplication()),
                        dateTime = requestItem.creationDate
                    )
                db.requestPhotoDao().insertRequestPhoto(requestPhoto)
            }
            onRequestSavedSuccess?.invoke("")
        }
    }

    /**
     * Добавление в БД информации для ТО
     * @param equipInfo - экземпляр информации для ТО [EquipInfo]
     */
    fun addEquipInfo(equipInfo: EquipInfo) {
        viewModelScope.launch {
            val equipInfoId = db.equipInfoDao().insertEquipInfo(equipInfo)
            onEquipInfoSavedSuccess?.invoke(equipInfoId)
        }
    }

    /**
     * Добавление в список URI фото или картинки для заявки
     * @param uri
     */
    fun addImage(uri: Uri) {
        val list = _uriList.value
        val mutableList = list?.toMutableList() ?: mutableListOf()
        mutableList.add(uri)
        _uriList.value = mutableList.toList()
    }

    /**
     * Удаление из списка URI фото
     * @param index
     */
    fun deleteImage(index: Int) {
        val list = _uriList.value
        val mutableList = list?.toMutableList() ?: mutableListOf()
        mutableList.removeAt(index)
        _uriList.value = mutableList.toList()
    }

    /**
     * Замена фото в списке при редактировании
     * @param index
     * @param uri
     */
    fun replaceImage(index: Int, uri: Uri) {
        val list = _uriList.value
        val mutableList = list?.toMutableList() ?: mutableListOf()
        mutableList.removeAt(index)
        mutableList.add(index, uri)
        _uriList.value = mutableList.toList()
    }
}