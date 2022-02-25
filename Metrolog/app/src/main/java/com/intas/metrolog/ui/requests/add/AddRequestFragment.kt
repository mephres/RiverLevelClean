package com.intas.metrolog.ui.requests.add

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
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
import com.intas.metrolog.databinding.FragmentBottomAddRequestBinding
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.equip_info_priority.EquipInfoPriority
import com.intas.metrolog.pojo.event.event_operation_type.EventOperationTypeItem
import com.intas.metrolog.pojo.event_comment.EventComment
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.ui.bottom_dialog.BottomDialogSheet
import com.intas.metrolog.ui.equip_document.adapter.ImageSliderViewAdapter
import com.intas.metrolog.ui.requests.add.adapter.CategorySpinnerAdapter
import com.intas.metrolog.ui.requests.add.adapter.DisciplineSpinnerAdapter
import com.intas.metrolog.ui.requests.add.adapter.OperationTypeSpinnerAdapter
import com.intas.metrolog.ui.requests.add.adapter.PrioritySpinnerAdapter
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util
import com.intas.metrolog.util.Util.YYYYMMDD_HHMMSS
import com.intas.metrolog.util.ViewUtil
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddRequestFragment : BottomSheetDialogFragment() {
    private var addRequestMode: String = MODE_UNKNOWN
    private var equip: EquipItem? = null
    private var isRequest = true

    private var requestImageFabVisible = false
    private var selectOperationType: EventOperationTypeItem? = null
    private var selectDiscipline: DisciplineItem? = null
    private var selectCategory: EventComment? = null
    private var selectPriority: EquipInfoPriority? = null

    private var operationTypeSpinnerAdapter: OperationTypeSpinnerAdapter? = null
    private var disciplineSpinnerAdapter: DisciplineSpinnerAdapter? = null
    private var categorySpinnerAdapter: CategorySpinnerAdapter? = null
    private var prioritySpinnerAdapter: PrioritySpinnerAdapter? = null
    private lateinit var imageSliderAdapter: ImageSliderViewAdapter

    private var photoURI: Uri? = null
    private var uriList: List<Uri>? = null

    private var cropImagePosition = 0

    private val binding by lazy {
        FragmentBottomAddRequestBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[AddRequestViewModel::class.java]
    }

    private val requestTypeArray = arrayOf(100, 200)
    private val modesArray = arrayOf(MODE_ADD_REQUEST_WITH_SCAN, MODE_ADD_REQUEST_WITHOUT_SCAN)

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

        setSpinnerAdapters()
        checkMode()
        binding.addRequestImageSliderView.setIndicatorVisibility(false)
        binding.addRequestImageFab.setOnClickListener {
            showRequestAttachImageFab()
        }

        binding.addRequestShowGalleryFab.setOnClickListener {
            selectImage()
        }

        binding.addRequestShowCameraFab.setOnClickListener {
            createPhoto()
        }

        binding.applyNewRequestButton.setOnClickListener {
            addRequest()
        }

        viewModel.uriList.observe(this) {
            binding.attachImageView.isVisible = it.isEmpty()
            uriList = it
            imageSliderAdapter = ImageSliderViewAdapter(it)
            binding.addRequestImageSliderView.setSliderAdapter(imageSliderAdapter)

            imageSliderAdapter.onCropImageListener = {
                editImage()
            }

            imageSliderAdapter.onDeleteImageListener = {
                deleteImage()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == -1) {
            when (requestCode) {
                UCrop.REQUEST_CROP -> {
                    data?.let { intentData ->

                        val imageUri = UCrop.getOutput(intentData)
                        try {
                            imageUri?.let {
                                viewModel.replaceImage(cropImagePosition, imageUri)
                            }
                        } catch (e: IOException) {
                            FirebaseCrashlytics.getInstance().recordException(e)
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
                        photoURI?.let {
                            viewModel.addImage(it)
                            showRequestAttachImageFab()
                        }
                    } catch (e: IOException) {
                        //FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                Util.GALLERY_REQUEST -> {
                    data?.let {
                        photoURI = data.data
                        photoURI?.let {
                            viewModel.addImage(it)
                            showRequestAttachImageFab()
                        }
                    }
                }
            }

        }
    }

    override fun onDestroyView() {

        super.onDestroyView()
    }

    private fun deleteUsedFiles() {
        uriList?.forEach {
            val file: File = File(it.getPath())
            file.delete()
            if (file.exists()) {
                try {
                    file.canonicalFile.delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (file.exists()) {
                    requireContext().deleteFile(file.name)
                }
            }
        }
    }

    private fun editImage() {

        ViewUtil.hideKeyboard(requireActivity())

        try {
            val timeStamp = SimpleDateFormat(YYYYMMDD_HHMMSS, Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(imageFileName, ".jpg", storageDir)

            //Сохраняем позицию редактируемого файла для передачи ее в активити
            cropImagePosition = binding.addRequestImageSliderView.getCurrentPagePosition()

            val options = UCrop.Options()
            options.setToolbarTitle("Редактирование")
            options.setLogoColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.design_default_color_background
                )
            )
            options.setCropFrameColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            options.setActiveControlsWidgetColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            options.setStatusBarColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimaryDark
                )
            )
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

        binding.addRequestImageSliderView.sliderAdapter?.let {

            if (it.count == 0) {
                return
            }

            val dialogSheet = BottomDialogSheet.newInstance(
                getString(R.string.equip_document_activity_delete_image_dialog_title),
                getString(R.string.equip_document_activity_delete_image_dialog_text),
                getString(R.string.equip_document_activity_delete_image_dialog_positive_button),
                getString(R.string.pequip_document_activity_delete_image_dialog_negative_button)
            )
            dialogSheet.show(
                requireActivity().supportFragmentManager,
                Util.BOTTOM_DIALOG_SHEET_FRAGMENT_TAG
            )
            dialogSheet.onPositiveClickListener = {
                val index = binding.addRequestImageSliderView.currentPagePosition
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
                ) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(
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
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
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

        val timeStamp = SimpleDateFormat(YYYYMMDD_HHMMSS, Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        return image
    }


    private fun addRequest() {
        if (selectCategory == null) {
            showToast(getString(R.string.add_request_need_category_title))
            return
        }

        if (isRequest && selectDiscipline == null) {
            showToast(getString(R.string.add_request_need_discipline_title))
            return
        }

        if (isRequest && selectOperationType == null) {
            showToast(getString(R.string.add_request_need_operation_title))
            return
        }

        if (!isRequest && selectPriority == null) {
            showToast(getString(R.string.add_request_need_priority_title))
            return
        }

        if (!isRequest && binding.addRequestCommentTextInputLayout.editText?.text?.trim()
                .isNullOrEmpty()
        ) {
            showToast(getString(R.string.add_equip_info_need_comment_title))
            return
        }

        if (isRequest) {
            createRequestItem()
        } else {
            createEquipInfoItem()
        }
    }

    private fun createRequestItem() {
        val senderId = Util.authUser?.userId ?: 0
        val discipline = selectDiscipline?.id ?: 0
        val operation = selectOperationType?.id ?: 0
        val category = selectCategory?.id ?: 0
        val equipId = equip?.equipId ?: -1
        val equipRfid = equip?.equipRFID ?: ""
        val comment = binding.addRequestCommentTextInputEditText.text.toString()

        val requestItem = RequestItem(
            senderId = senderId,
            discipline = discipline,
            operationType = operation,
            typeRequest = switchIsChecked(),
            comment = comment,
            categoryId = category,
            creationDate = DateTimeUtil.getUnixDateTimeNow(),
            equipId = equipId.toString(),
            rfid = equipRfid,
            status = 1,
            isSended = 0
        )
        viewModel.addRequest(requestItem)
        binding.progressBar.visibility = View.VISIBLE
        viewModel.onRequestSavedSuccess = {
            Journal.insertJournal("AddRequestFragment->createRequest", requestItem)
            showToast(getString(R.string.add_request_success_title))
            closeFragment()
        }
    }

    private fun createEquipInfoItem() {
        val comment = binding.addRequestCommentTextInputEditText.text.toString()
        val priority = selectPriority?.id
        val equipId = equip?.equipId ?: -1

        val equipInfo = EquipInfo(
            equipId = equipId,
            text = comment,
            priority = priority ?: 0,
            isSended = 0,
            dateTime = DateTimeUtil.getUnixDateTimeNow()
        )

        viewModel.addEquipInfo(equipInfo)
        binding.progressBar.visibility = View.VISIBLE
        viewModel.onEquipInfoSavedSuccess = {
            Journal.insertJournal("AddRequestFragment->createEquipInfo", equipInfo)
            showToast(getString(R.string.add_equip_info_success_title))
            closeFragment()
        }
    }

    private fun setSpinnerAdapters() {
        viewModel.operationTypes.observe(viewLifecycleOwner, {
            operationTypeSpinnerAdapter = OperationTypeSpinnerAdapter(
                requireContext(),
                R.layout.drop_down_list_item,
                it
            )
            (binding.addRequestOperationTypeMenu.editText as? AutoCompleteTextView)?.setAdapter(
                operationTypeSpinnerAdapter
            )
        })

        viewModel.disciplines.observe(viewLifecycleOwner, {
            disciplineSpinnerAdapter = DisciplineSpinnerAdapter(
                requireContext(),
                R.layout.drop_down_list_item,
                it
            )
            (binding.addRequestDisciplineMenu.editText as? AutoCompleteTextView)?.setAdapter(
                disciplineSpinnerAdapter
            )
        })

        viewModel.category.observe(viewLifecycleOwner, {
            val categoryList = it.filter { requestTypeArray.contains(it.type) }.toMutableList()
            categorySpinnerAdapter = if (equip != null) {
                CategorySpinnerAdapter(
                    requireContext(),
                    R.layout.drop_down_list_item,
                    categoryList
                )
            } else {
                CategorySpinnerAdapter(
                    requireContext(),
                    R.layout.drop_down_list_item,
                    categoryList.filter { it.type == 100 }
                )
            }

            (binding.addRequestCategoryMenu.editText as? AutoCompleteTextView)?.setAdapter(
                categorySpinnerAdapter
            )
        })

        viewModel.priority.observe(viewLifecycleOwner, {
            prioritySpinnerAdapter = PrioritySpinnerAdapter(
                requireContext(),
                R.layout.drop_down_list_item,
                it
            )
            (binding.addRequestPriorityMenu.editText as? AutoCompleteTextView)?.setAdapter(
                prioritySpinnerAdapter
            )
        })

        configureOperationsSpinner()
        configureDisciplinesSpinner()
        configureCategorySpinner()
        configurePrioritySpinner()
    }

    private fun configureOperationsSpinner() {
        binding.addRequestOperationTypeList.setOnItemClickListener { _, _, position, _ ->
            selectOperationType = operationTypeSpinnerAdapter?.getItem(position)
            selectOperationType?.let {
                binding.addRequestOperationTypeList.setText(it.name)
            }
        }
    }

    private fun configureDisciplinesSpinner() {
        binding.addRequestDisciplineList.setOnItemClickListener { _, _, position, _ ->
            selectDiscipline = disciplineSpinnerAdapter?.getItem(position)
            selectDiscipline?.let {
                binding.addRequestDisciplineList.setText(it.name)
            }
        }
    }

    private fun configurePrioritySpinner() {
        binding.addRequestPriorityList.setOnItemClickListener { _, _, position, _ ->
            selectPriority = prioritySpinnerAdapter?.getItem(position)
            selectPriority?.let {
                binding.addRequestPriorityList.setText(it.name)
            }
        }
    }

    private fun configureCategorySpinner() {
        binding.addRequestCategoryList.setOnItemClickListener { _, _, position, _ ->
            selectCategory = categorySpinnerAdapter?.getItem(position)
            selectCategory?.let {
                binding.newRequestTitleTextView.text = it.comment
                binding.addRequestCategoryList.setText(it.comment)
                binding.addRequestCommentTextInputLayout.hint = "${it.comment}. Описание"

                if (it.type == 200) {
                    isRequest = false

                    binding.imageListCardView.visibility = View.GONE
                    binding.addRequestPriorityMenu.visibility = View.VISIBLE
                    binding.requestTypeSwitch.visibility = View.GONE
                    binding.requestTypeTitleTextView.visibility = View.GONE
                } else {
                    isRequest = true

                    binding.imageListCardView.visibility = View.VISIBLE
                    binding.addRequestPriorityMenu.visibility = View.GONE
                    binding.requestTypeSwitch.visibility = View.VISIBLE
                    binding.requestTypeTitleTextView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showRequestAttachImageFab() {

        requestImageFabVisible = !requestImageFabVisible

        binding.addRequestShowGalleryFab.visibility = View.INVISIBLE
        binding.addRequestShowCameraFab.visibility = View.INVISIBLE

        if (requestImageFabVisible) {
            binding.addRequestShowGalleryFab.visibility = View.VISIBLE
            binding.addRequestShowCameraFab.visibility = View.VISIBLE
        }
    }

    private fun switchIsChecked(): Int {
        return if (binding.requestTypeSwitch.isChecked) {
            1
        } else 0
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(ADD_REQUEST_MODE)) {
            return
        }

        val mode = args.getString(ADD_REQUEST_MODE)
        if (!modesArray.contains(mode)) {
            return
        }
        mode?.let { addRequestMode = it }

        if (addRequestMode == MODE_ADD_REQUEST_WITH_SCAN) {
            if (!args.containsKey(EQUIP_ITEM)) {
                return
            }
            equip = args.getParcelable(EQUIP_ITEM)

            if (equip == null) {
                return
            }
        }
    }

    private fun checkMode() {
        when (addRequestMode) {
            MODE_ADD_REQUEST_WITH_SCAN -> {
                binding.equipInfoTextView.visibility = View.VISIBLE
                binding.equipInfoTextView.text = equip?.equipName
                Journal.insertJournal("AddRequestFragment->addRequestWithScan",  "$equip")
            }

            MODE_ADD_REQUEST_WITHOUT_SCAN -> {
                showToast(getString(R.string.add_request_no_equip_message_title))
                Journal.insertJournal("AddRequestFragment->addRequestWithoutScan", "")
            }
        }
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(ADD_REQUEST_FRAGMENT_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ADD_REQUEST_FRAGMENT_TAG = "add_request_fragment_tag"

        private const val MODE_UNKNOWN = "unknown_mode"
        private const val ADD_REQUEST_MODE = "add_request_mode"
        private const val MODE_ADD_REQUEST_WITH_SCAN = "mode_add_request_with_scan"
        private const val MODE_ADD_REQUEST_WITHOUT_SCAN = "mode_add_request_without_scan"

        private const val EQUIP_ITEM = "equip_item"

        fun newInstanceWithRfid(equip: EquipItem) = AddRequestFragment().apply {
            arguments = Bundle().apply {
                putString(ADD_REQUEST_MODE, MODE_ADD_REQUEST_WITH_SCAN)
                putParcelable(EQUIP_ITEM, equip)
            }
        }

        fun newInstanceWithoutRfid() = AddRequestFragment().apply {
            arguments = Bundle().apply {
                putString(ADD_REQUEST_MODE, MODE_ADD_REQUEST_WITHOUT_SCAN)
            }
        }
    }
}