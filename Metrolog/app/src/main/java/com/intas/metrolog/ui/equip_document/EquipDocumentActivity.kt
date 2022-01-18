package com.intas.metrolog.ui.equip_document

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.BuildConfig
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityEquipDocumentBinding
import com.intas.metrolog.pojo.document_type.DocumentType
import com.intas.metrolog.pojo.equip.EquipDocument
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.bottom_dialog.BottomDialogSheet
import com.intas.metrolog.ui.equip_document.adapter.DocumentTypeAdapter
import com.intas.metrolog.ui.equip_document.adapter.ImageSliderViewAdapter
import com.intas.metrolog.util.Util
import com.intas.metrolog.util.Util.Companion.CAMERA_CAPTURE
import com.intas.metrolog.util.Util.Companion.GALLERY_REQUEST
import com.intas.metrolog.util.Util.Companion.YYYYMMDD_HHMMSS
import com.intas.metrolog.util.ViewUtil
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EquipDocumentActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityEquipDocumentBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[EquipDocumentViewModel::class.java]
    }

    private lateinit var imageSliderAdapter: ImageSliderViewAdapter

    private lateinit var documentTypeAdapter: DocumentTypeAdapter

    private lateinit var equipItem: EquipItem
    private var selectDocumentType: DocumentType? = null
    private var photoURI: Uri? = null
    private var photoPath: String? = null
    private var uriList: List<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.title = "Генератор PDF"
        val intent = intent
        if (intent != null) {
            equipItem = intent.getParcelableExtra<EquipItem>(EXTRA_EQUIP_ITEM) as EquipItem
        }
        supportActionBar?.let { actionBar ->
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        this,
                        R.color.colorAccent
                    )
                )
            )
            //init()
        }

        viewModel.documentTypeList.observe(this, {
            //Journal.insertJournal("AddTaskFragment->fieldList", list = it)
            documentTypeAdapter =
                DocumentTypeAdapter(this, R.layout.drop_down_list_item, it)
            (binding.equipDocumentTypeSpinner.editText as? AutoCompleteTextView)?.setAdapter(
                documentTypeAdapter
            )
        })

        configureDocumentTypeSpinner()


        binding.equipDocumentPhotoButton.setOnClickListener {
            createPhoto()
        }

        binding.equipDocumentChoosePhotoButton.setOnClickListener {
            selectImage()
        }

        binding.equipDocumentDeletePhotoButton.setOnClickListener {
            deleteImage()
        }

        binding.equipDocumentEditPhotoButton.setOnClickListener {
            editImage()
        }

        binding.equipDocumentSavePdfButton.setOnClickListener {
            generatePDF()
        }

        viewModel.uriList.observe(this) {
            uriList = it
            imageSliderAdapter = ImageSliderViewAdapter(it)
            binding.equipDocumentImageSliderView.setSliderAdapter(imageSliderAdapter)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun generatePDF() {

        if (selectDocumentType == null) {
            Toast.makeText(this, "Необходимо выбрать тип документа", Toast.LENGTH_LONG).show()
        }

        Util.safeLet(uriList, selectDocumentType) { uriList, selectDocumentType ->
            viewModel.generatePDF(uriList, equipItem.equipId, selectDocumentType)
        }

        viewModel.onSavePDFCompleted = {
            showPDFViewDialog(it)
        }
    }

    private fun showPDFViewDialog(equipDocument: EquipDocument) {

        val dialogSheet = BottomDialogSheet.newInstance(
            getString(R.string.equip_document_activity_pdf_view_dialog_title),
            String.format(
                getString(R.string.equip_document_activity_pdf_view_dialog_text),
                selectDocumentType?.name,
                equipItem.equipName
            ),
            getString(R.string.equip_document_activity_pdf_view_dialog_positive_button),
            getString(R.string.pequip_document_activity_pdf_view_dialog_negative_button)
        )
        dialogSheet.isCancelable = false
        dialogSheet.show(supportFragmentManager, Util.BOTTOM_DIALOG_SHEET_FRAGMENT_TAG)
        dialogSheet.onPositiveClickListener = {
            try {

                equipDocument.filePath?.let { path ->
                    viewPDF(path)
                }

                clearAll()
            } catch (e: Exception) {
                showToast("При открытии файла произошла ошибка. " + e.message)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }

        dialogSheet.onNegativeClickListener = {
            clearAll()
        }
    }

    private fun editImage() {

        ViewUtil.hideKeyboard(this)

        try {
            val timeStamp = SimpleDateFormat(YYYYMMDD_HHMMSS, Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(imageFileName, ".jpg", storageDir)

            //Сохраняем позицию редактируемого файла для передачи ее в активити
            val position = binding.equipDocumentImageSliderView.getCurrentPagePosition()

            val options = UCrop.Options()
            options.setToolbarTitle("Редактирование")
            options.setLogoColor(
                ContextCompat.getColor(
                    this,
                    R.color.design_default_color_background
                )
            )
            options.setCropFrameColor(ContextCompat.getColor(this, R.color.colorPrimary))
            options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary))
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            options.setToolbarColor(
                ContextCompat.getColor(
                    this,
                    R.color.design_default_color_background
                )
            )
            options.setRootViewBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.design_default_color_background
                )
            )
            options.setCropFrameColor(
                ContextCompat.getColor(
                    this,
                    R.color.design_default_color_background
                )
            )
            uriList?.let {
                UCrop.of(it[position], Uri.fromFile(image))
                    .withOptions(options)
                    .start(this)
            }

        } catch (e: java.lang.Exception) {
            //showErrorToast("Ошибка при создании файла - " + e.message)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun deleteImage() {

        binding.equipDocumentImageSliderView.sliderAdapter?.let {

            if (it.count == 0) {
                return
            }

            val dialogSheet = BottomDialogSheet.newInstance(
                getString(R.string.equip_document_activity_delete_image_dialog_title),
                getString(R.string.equip_document_activity_delete_image_dialog_text),
                getString(R.string.equip_document_activity_delete_image_dialog_positive_button),
                getString(R.string.pequip_document_activity_delete_image_dialog_negative_button)
            )
            dialogSheet.show(supportFragmentManager, Util.BOTTOM_DIALOG_SHEET_FRAGMENT_TAG)
            dialogSheet.onPositiveClickListener = {
                val index = binding.equipDocumentImageSliderView.getCurrentPagePosition()
                viewModel.deleteImage(index)
            }
        }
    }

    /**
     * Открытие галереи с изображениями
     */
    private fun selectImage() {
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    /**
     * Создание фото
     */
    private fun createPhoto() {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
            ) {
                showToast(getString(R.string.no_permission_camera_message))
                return
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                showToast(getString(R.string.no_permission_camera_message))
                return
            }
            showCamera()
        } catch (cant: ActivityNotFoundException) {
            showToast(getString(R.string.no_support_camera_message))
        }
    }

    /**
     * Открытие камеры телефона
     */
    private fun showCamera() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Убедитесь, что есть активность камеры для обработки намерения
        if (intent.resolveActivity(this.packageManager) != null) {
            // Создайте файл, в котором должно быть фото
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Ошибка при создании файла
                //FirebaseCrashlytics.getInstance().recordException(ex)
            }
            // Продолжить, только если файл был успешно создан
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, CAMERA_CAPTURE)
            }
        }
    }

    /**
     * Создание файла с изображением
     * @return файл с изображением
     * @throws IOException ошибка при создании файла для дальнейшей обработки
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {

        val timeStamp = SimpleDateFormat(YYYYMMDD_HHMMSS, Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

        // Сохранить файл: путь для использования с намерениями ACTION_VIEW
        photoPath = image.absolutePath
        return image
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun configureDocumentTypeSpinner() {
        binding.equipDocumentTypeList.setOnItemClickListener { parent, view, position, id ->
            selectDocumentType = documentTypeAdapter?.getItem(position)
            selectDocumentType?.let {
                binding.equipDocumentTypeList.setText(it.name)
            }
            ViewUtil.hideKeyboard(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == -1) {
            var imageBitmap: Bitmap? = null

            when (requestCode) {
                CAMERA_CAPTURE -> {
                    try {
                        imageBitmap =
                            MediaStore.Images.Media.getBitmap(contentResolver, photoURI)
                        photoURI?.let { viewModel.addImage(it) }

                    } catch (e: IOException) {
                        //FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                GALLERY_REQUEST -> {
                    data?.let {
                        photoURI = data.data
                        photoURI?.let { viewModel.addImage(it) }
                    }
                }
            }

        }
    }

    private fun viewPDF(filepath: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val file = File(filepath)
        val localUri =
            FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.setDataAndType(localUri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun clearAll() {
        uriList = emptyList()
        imageSliderAdapter = ImageSliderViewAdapter(emptyList())
        binding.equipDocumentImageSliderView.setSliderAdapter(imageSliderAdapter)
        binding.equipDocumentTypeList.text.clear()
        selectDocumentType = null
    }

    companion object {

        private const val EXTRA_EQUIP_ITEM = "equip_item"

        fun newIntent(context: Context, equipItem: EquipItem): Intent {
            val intent = Intent(context, EquipDocumentActivity::class.java)
            intent.putExtra(EXTRA_EQUIP_ITEM, equipItem)
            return intent
        }
    }
}