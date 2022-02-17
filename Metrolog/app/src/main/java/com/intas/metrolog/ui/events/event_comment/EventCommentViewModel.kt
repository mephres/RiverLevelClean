package com.intas.metrolog.ui.events.event_comment

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.event.event_photo.EventPhotoItem
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class EventCommentViewModel(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable = CompositeDisposable()

    private val db = AppDatabase.getInstance(application)
    private val context = application.applicationContext

    var onEventPhotoSavedSuccess: ((Long) -> Unit)? = null

    private var _uriList = MutableLiveData<List<Uri>>()
    val uriList: LiveData<List<Uri>>
        get() = _uriList

    fun addImage(uri: Uri) {
        val list = _uriList.value
        val mutableList = list?.toMutableList() ?: mutableListOf()
        mutableList.add(uri)
        _uriList.value = mutableList.toList()
    }

    fun deleteImage(index: Int) {
        val list = _uriList.value
        val mutableList = list?.toMutableList() ?: mutableListOf()
        mutableList.removeAt(index)
        _uriList.value = mutableList.toList()
    }

    fun replaceImage(index: Int, uri: Uri) {
        val list = _uriList.value
        val mutableList = list?.toMutableList() ?: mutableListOf()
        mutableList.removeAt(index)
        mutableList.add(index, uri)
        _uriList.value = mutableList.toList()
    }

    fun saveEventPhoto(uriList: List<Uri>, eventId: Long) {
        viewModelScope.launch {
            uriList.forEach {
                val eventComment = EventPhotoItem(opId = eventId, photoUri = it.toString())
                db.eventPhotoDao().insertEventPhoto(eventComment)
            }
            onEventPhotoSavedSuccess?.invoke(eventId)
        }
    }
}