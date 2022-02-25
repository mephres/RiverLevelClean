package com.intas.metrolog.ui.equip_document

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.R
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.document_type.DocumentType
import com.intas.metrolog.pojo.equip.EquipDocument
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util.YYYYMMDD_HHMMSS
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

    var onSavePDFCompleted: ((EquipDocument) -> Unit)? = null

    //список типов документа для оборудования
    val documentTypeList = db.documentTypeDao().getDocumentTypes()

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

    fun generatePDF(uriList: List<Uri>, equipId: Long, documentType: DocumentType) {
        viewModelScope.launch {

            val document = PdfDocument()
            var pageCount = 0
            uriList.forEach { uri ->
                val imageBitmap =  MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                imageBitmap?.let { imageBitmap->
                    var tempWidth = 0
                    var tempHeight = 0
                    try {
                        tempWidth = imageBitmap.getWidth() / 1 //Util.getInstance().getImageSizeScalePDF()
                        tempHeight = imageBitmap.getHeight() / 1//Util.getInstance().getImageSizeScalePDF()
                    } catch (e: Exception) {
                        tempWidth = 240
                        tempHeight = 320
                    }

                    val pageInfo = PageInfo.Builder(tempWidth + 50, tempHeight + 50, pageCount + 1).create()
                    val page = document.startPage(pageInfo)

                    val canvas = page.canvas

                    val paint = Paint()
                    paint.color = ContextCompat.getColor(context, R.color.md_white)
                    canvas.drawPaint(paint)

                    val bitmap = Bitmap.createScaledBitmap(imageBitmap, tempWidth, tempHeight, true)

                    paint.color = ContextCompat.getColor(context, R.color.colorAccent)
                    canvas.drawBitmap(bitmap, 25f, 25f, paint)

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
                    filePath = targetPDF
                )

                db.equipDocumentDao().insertEquipDocument(equipDocument)
                onSavePDFCompleted?.invoke(equipDocument)
                Journal.insertJournal("EquipDocumentViewModel->generatePDF", equipDocument)
            } catch (e: IOException) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            // закрываем документ
            document.close()
        }
    }
}