package com.intas.metrolog.ui.equip_document

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.document_type.DocumentType
import com.intas.metrolog.pojo.equip.EquipDocument
import com.intas.metrolog.util.Util.Companion.YYYYMMDD_HHMMSS
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EquipDocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable = CompositeDisposable()

    private val db = AppDatabase.getInstance(application)
    private val context = application.applicationContext

    //список типов документа для оборудования
    val documentTypeList = db.documentTypeDao().getDocumentTypes()

    var _uriList = MutableLiveData<List<Uri>>()
    val uriList: LiveData<List<Uri>>
        get() = _uriList

    var _savePDFCompleted = MutableLiveData<Boolean>()
    val savePDFCompleted: LiveData<Boolean>
        get() = _savePDFCompleted

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

    fun generatePDF(uriList: List<Uri>, equipId: Long, documentType: DocumentType) {
        viewModelScope.launch {
            _savePDFCompleted.value = false

            val document = PdfDocument()
            var pageCount = 0
            uriList.forEach { uri ->
                val imageBitmap =  MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                imageBitmap?.let { bitmap->
                    var tempWidth = 0
                    var tempHeight = 0
                    try {
                        tempWidth = bitmap.getWidth() / 1 //Util.getInstance().getImageSizeScalePDF()
                        tempHeight = bitmap.getHeight() / 1//Util.getInstance().getImageSizeScalePDF()
                    } catch (e: Exception) {
                        tempWidth = 240
                        tempHeight = 320
                    }

                    val pageInfo = PageInfo.Builder(tempWidth + 50, tempHeight + 50, pageCount + 1).create()
                    val page = document.startPage(pageInfo)

                    val canvas = page.canvas

                    val paint = Paint()
                    paint.color = Color.parseColor("#ffffff")
                    canvas.drawPaint(paint)

                    var bitmap = Bitmap.createScaledBitmap(bitmap, tempWidth, tempHeight, true)

                    paint.color = Color.BLUE
                    canvas.drawBitmap(bitmap, 25f, 25f, null)

                    if (bitmap != null) {
                        bitmap = null
                    }
                    document.finishPage(page)
                    pageCount++
                }
            }

            // Записываем содержимое документа
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val timeStamp = SimpleDateFormat(YYYYMMDD_HHMMSS, Locale.getDefault()).format(Date())
            val fileName = "${equipId}_${documentType.code}_$timeStamp.pdf"
            val targetPDF = "$storageDir/$fileName"

            val filePath = File(targetPDF)
            try {
                document.writeTo(FileOutputStream(filePath))
                val equipDocument = EquipDocument(
                    equipId = equipId,
                    documentTypeId = documentType.id,
                    filename = fileName,
                    filePath = Objects.requireNonNull(storageDir)?.path
                )

                db.equipDocumentDao().insertEquipDocument(equipDocument)
                _savePDFCompleted.value = true
            } catch (e: IOException) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            // закрываем документ
            document.close()
        }
    }
}