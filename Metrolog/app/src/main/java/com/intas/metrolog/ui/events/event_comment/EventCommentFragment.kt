package com.intas.metrolog.ui.events.event_comment

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.BuildConfig
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEventCommentBinding
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.CANCELED
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.COMPLETED
import com.intas.metrolog.ui.bottom_dialog.BottomDialogSheet
import com.intas.metrolog.ui.equip_document.adapter.ImageSliderViewAdapter
import com.intas.metrolog.util.FileUtil
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util
import com.intas.metrolog.util.ViewUtil
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EventCommentFragment : BottomSheetDialogFragment() {

    var onSaveCommentListener: ((String, Int) -> Unit)? = null

    private lateinit var imageSliderAdapter: ImageSliderViewAdapter

    private var eventCommentMode: String = MODE_UNKNOWN
    private var eventId: Long = 0
    private var eventStatus: Int = 0
    private val modesArray = arrayOf(
        MODE_COMMENT_WITH_IMAGE,
        MODE_COMMENT_WITHOUT_IMAGE
    )
    private var photoURI: Uri? = null
    private var photoPath: String? = null
    private var uriList: List<Uri>? = null
    private var imageFabVisible: Boolean = false

    private var cropImagePosition = 0

    private val binding by lazy {
        FragmentEventCommentBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[EventCommentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkMode()
        initClickListeners()
        initObservers()
        fillComment()
    }

    private fun initObservers() {
        binding.eventCommentImageSliderView.setIndicatorVisibility(false)
        viewModel.uriList.observe(this) {
            binding.attachImageView.isVisible = it.isEmpty()
            uriList = it
            imageSliderAdapter = ImageSliderViewAdapter(it)
            binding.eventCommentImageSliderView.setSliderAdapter(imageSliderAdapter)

            imageSliderAdapter.onCropImageListener = {
                editImage()
            }

            imageSliderAdapter.onDeleteImageListener = {
                deleteImage()
            }
        }
    }

    private fun initClickListeners() {
        binding.eventCommentImageFab.setOnClickListener {
            showAttachImageFab()
        }

        binding.eventCommentPhotoButton.setOnClickListener {
            createPhoto()
            showAttachImageFab()
        }

        binding.eventCommentChoosePhotoButton.setOnClickListener {
            selectImage()
            showAttachImageFab()
        }

        binding.saveEventCommentButton.setOnClickListener {
            saveComment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun saveComment() {

        val comment = binding.eventCommentTextInputLayout.editText?.text.toString()
        if (comment.isEmpty()) {
            showToast(getString(R.string.event_comment_add_comment_error_message))
            return
        }

        if (eventCommentMode.equals(MODE_COMMENT_WITH_IMAGE) && uriList.isNullOrEmpty()) {
            showToast(getString(R.string.event_comment_add_comment_empty_image_list_error_message))
            return
        }
        uriList?.let {
            viewModel.saveEventPhoto(it, eventId)
        }

        when (eventCommentMode) {
            MODE_COMMENT_WITH_IMAGE -> {
                viewModel.onEventPhotoSavedSuccess = {
                    onSaveCommentListener?.invoke(comment, eventStatus)
                    closeFragment()
                }
            }

            MODE_COMMENT_WITHOUT_IMAGE -> {
                onSaveCommentListener?.invoke(comment, eventStatus)
                closeFragment()
            }
        }
    }

    private fun fillComment() {
        var textStatus = ""
        when(eventStatus) {
            COMPLETED ->  textStatus = "Выполнено"
            CANCELED -> textStatus = "Отменено"
            else -> textStatus = ""
        }
        binding.eventCommentTextInputLayout.editText?.setText(textStatus)
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(EVENT_COMMENT_MODE)) {
            return
        }

        val mode = args.getString(EVENT_COMMENT_MODE)
        if (!modesArray.contains(mode)) {
            return
        }
        mode?.let { eventCommentMode = it }

        if (eventCommentMode.equals(MODE_COMMENT_WITHOUT_IMAGE) || eventCommentMode.equals(MODE_COMMENT_WITH_IMAGE)) {
            if (!args.containsKey(EVENT_ID) || !args.containsKey(EVENT_STATUS)) {
                return
            }
            eventId = args.getLong(EVENT_ID)
            eventStatus = args.getInt(EVENT_STATUS)
        }
    }

    private fun checkMode() {
        when (eventCommentMode) {
            MODE_COMMENT_WITH_IMAGE -> {
                FileUtil.setContext(requireActivity().application)
                binding.imageListCardView.visibility = View.VISIBLE
                Journal.insertJournal("EventCommentFragment->checkMode", "commentWithImage")
            }

            MODE_COMMENT_WITHOUT_IMAGE -> {
                binding.imageListCardView.visibility = View.GONE
                Journal.insertJournal("EventCommentFragment->checkMode", "commentWithoutImage")
            }
        }
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(EVENT_COMMENT_FRAGMENT_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun showAttachImageFab() {
        imageFabVisible = !imageFabVisible

        binding.eventCommentPhotoButton.visibility = View.INVISIBLE
        binding.eventCommentChoosePhotoButton.visibility = View.INVISIBLE

        if (imageFabVisible) {
            binding.eventCommentPhotoButton.visibility = View.VISIBLE
            binding.eventCommentChoosePhotoButton.visibility = View.VISIBLE
        }
    }

    private fun editImage() {

        ViewUtil.hideKeyboard(requireActivity())

        try {
            val timeStamp = SimpleDateFormat(Util.YYYYMMDD_HHMMSS, Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(imageFileName, ".jpg", storageDir)

            //Сохраняем позицию редактируемого файла для передачи ее в активити
            cropImagePosition = binding.eventCommentImageSliderView.getCurrentPagePosition()

            val options = UCrop.Options()
            options.setToolbarTitle("Редактирование")
            options.setLogoColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.design_default_color_background
                )
            )
            options.setCropFrameColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            options.setActiveControlsWidgetColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            options.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
            options.setToolbarColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.design_default_color_background
                )
            )
            options.setRootViewBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.design_default_color_background
                )
            )
            options.setCropFrameColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.design_default_color_background
                )
            )
            uriList?.let {
                UCrop.of(it[cropImagePosition], Uri.fromFile(image))
                    .withOptions(options)
                    .start(requireActivity(), this)
            }

        } catch (e: java.lang.Exception) {
            //showErrorToast("Ошибка при создании файла - " + e.message)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun deleteImage() {

        binding.eventCommentImageSliderView.sliderAdapter?.let {

            if (it.count == 0) {
                return
            }

            val dialogSheet = BottomDialogSheet.newInstance(
                getString(R.string.equip_document_activity_delete_image_dialog_title),
                getString(R.string.equip_document_activity_delete_image_dialog_text),
                getString(R.string.equip_document_activity_delete_image_dialog_positive_button),
                getString(R.string.pequip_document_activity_delete_image_dialog_negative_button)
            )
            dialogSheet.show(requireActivity().supportFragmentManager, Util.BOTTOM_DIALOG_SHEET_FRAGMENT_TAG)
            dialogSheet.onPositiveClickListener = {
                val index = binding.eventCommentImageSliderView.getCurrentPagePosition()
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        intent.type = "image/*"
        startActivityForResult(intent, Util.GALLERY_REQUEST)
    }

    /**
     * Создание фото
     */
    private fun createPhoto() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
            ) {
                showToast(getString(R.string.no_permission_camera_message))
                return
            }
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
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
        if (intent.resolveActivity(requireContext().packageManager) != null) {
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
                    requireContext(),
                    "${BuildConfig.APPLICATION_ID}.provider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, Util.CAMERA_CAPTURE)
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

        val timeStamp = SimpleDateFormat(Util.YYYYMMDD_HHMMSS, Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

        // Сохранить файл: путь для использования с намерениями ACTION_VIEW
        photoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == -1) {
            var imageBitmap: Bitmap? = null

            when (requestCode) {
                UCrop.REQUEST_CROP -> {
                    data?.let { intentData ->
                        val imageUri = UCrop.getOutput(intentData)
                        imageUri?.let {
                            val imagePath = FileUtil.getPath(it)
                            var localUri: Uri? = null

                            imagePath?.let {
                                localUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    "${BuildConfig.APPLICATION_ID}.provider",
                                    File(imagePath)
                                )
                            }

                            localUri?.let {
                                viewModel.replaceImage(cropImagePosition, it)
                            }
                        }
                    }
                }
                UCrop.RESULT_ERROR -> {
                    data?.let {
                        val cropError = UCrop.getError(it)
                        showToast("Ошибка при редактировании изображения - " + cropError?.message)
                    }
                }
                Util.CAMERA_CAPTURE -> {
                    try {
                        imageBitmap =
                            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, photoURI)
                        photoURI?.let { viewModel.addImage(it) }

                    } catch (e: IOException) {
                        //FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                Util.GALLERY_REQUEST -> {
                    data?.data?.let {
                        val imagePath = FileUtil.getPath(it)
                        var localUri: Uri? = null

                        imagePath?.let {
                            localUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${BuildConfig.APPLICATION_ID}.provider",
                                File(imagePath)
                            )
                        }

                        localUri?.let {
                            viewModel.addImage(it)
                        }

                    }

                    data?.clipData?.let {
                        for (i in 0 until it.itemCount) {
                            val requestUri = it.getItemAt(i).uri
                            val imagePath = FileUtil.getPath(requestUri)
                            var localUri: Uri? = null

                            imagePath?.let {
                                localUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    "${BuildConfig.APPLICATION_ID}.provider",
                                    File(imagePath)
                                )
                            }

                            localUri?.let {
                                viewModel.addImage(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EVENT_COMMENT_FRAGMENT_TAG = "event_comment_fragment_tag"

        private const val MODE_UNKNOWN = "unknown_mode"
        private const val EVENT_COMMENT_MODE = "event_comment_mode"
        private const val MODE_COMMENT_WITH_IMAGE = "mode_comment_with_image"
        private const val MODE_COMMENT_WITHOUT_IMAGE = "mode_comment_without_image"

        private const val EVENT_ID = "event_id"
        private const val EVENT_STATUS = "event_status"

        fun newInstanceWithImage(eventId: Long, eventStatus: Int) = EventCommentFragment().apply {
            arguments = Bundle().apply {
                putString(EVENT_COMMENT_MODE, MODE_COMMENT_WITH_IMAGE)
                putLong(EVENT_ID, eventId)
                putInt(EVENT_STATUS, eventStatus)
            }
        }

        fun newInstanceWithoutImage(eventId: Long, eventStatus: Int) =
            EventCommentFragment().apply {
                arguments = Bundle().apply {
                    putString(EVENT_COMMENT_MODE, MODE_COMMENT_WITHOUT_IMAGE)
                    putLong(EVENT_ID, eventId)
                    putInt(EVENT_STATUS, eventStatus)
                }
            }
    }
}