package com.intas.metrolog.ui.requests.add

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentBottomAddRequestBinding
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.equip_info_priority.EquipInfoPriority
import com.intas.metrolog.pojo.event_comment.EventComment
import com.intas.metrolog.pojo.operation.EventOperationItem
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.ui.requests.add.adapter.CategorySpinnerAdapter
import com.intas.metrolog.ui.requests.add.adapter.DisciplineSpinnerAdapter
import com.intas.metrolog.ui.requests.add.adapter.OperationSpinnerAdapter
import com.intas.metrolog.ui.requests.add.adapter.PrioritySpinnerAdapter
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util

class AddRequestFragment : BottomSheetDialogFragment() {
    private var addRequestMode: String = MODE_UNKNOWN
    private var equip: EquipItem? = null
    private var isRequest = true

    private var requestImageFabVisible = false
    private var selectOperation: EventOperationItem? = null
    private var selectDiscipline: DisciplineItem? = null
    private var selectCategory: EventComment? = null
    private var selectPriority: EquipInfoPriority? = null

    private var operationSpinnerAdapter: OperationSpinnerAdapter? = null
    private var disciplineSpinnerAdapter: DisciplineSpinnerAdapter? = null
    private var categorySpinnerAdapter: CategorySpinnerAdapter? = null
    private var prioritySpinnerAdapter: PrioritySpinnerAdapter? = null


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

        binding.addRequestImageFab.setOnClickListener {
            showRequestAttachImageFab()
        }

        binding.addRequestShowGalleryFab.setOnClickListener {

        }

        binding.addRequestShowCameraFab.setOnClickListener {

        }

        binding.applyNewRequestButton.setOnClickListener {
            addRequest()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun addRequest() {
        if (selectCategory == null) {
            binding.addRequestCategoryMenu.error = getString(R.string.add_request_need_category_title)
            return
        } else {
            binding.addRequestCategoryMenu.error = null
        }

        if (!isRequest && selectDiscipline == null) {
            binding.addRequestDisciplineMenu.error = getString(R.string.add_request_need_discipline_title)
            return
        } else {
            binding.addRequestDisciplineMenu.error = null
        }

        if (!isRequest && selectOperation == null) {
            binding.addRequestOperationMenu.error = getString(R.string.add_request_need_operation_title)
            return
        } else {
            binding.addRequestOperationMenu.error = null
        }

        if (!isRequest && selectPriority == null) {
            binding.addRequestPriorityMenu.error = getString(R.string.add_request_need_priority_title)
            return
        } else {
            binding.addRequestPriorityMenu.error = null
        }

        if (!isRequest && binding.addRequestCommentTextInputLayout.editText?.text?.trim().isNullOrEmpty()) {
            binding.addRequestCommentTextInputLayout.error = getString(R.string.add_request_need_comment_title)
            return
        } else {
            binding.addRequestCommentTextInputLayout.error = null
        }

        createRequestItem()
    }

    private fun createRequestItem() {
        val senderId = Util.authUser?.userId ?: 0
        val discipline = selectDiscipline?.id ?: 0
        val operation = selectOperation?.id ?: 0
        val category = selectCategory?.id ?: 0
        val equipId = equip?.equipId?.toInt() ?: -1
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
            equipId = equipId,
            rfid = equipRfid,
            status = 1
        )
        viewModel.addRequest(requestItem)
        closeFragment()
    }

    private fun setSpinnerAdapters() {
        viewModel.operations.observe(viewLifecycleOwner, {
            operationSpinnerAdapter = OperationSpinnerAdapter(
                requireContext(),
                R.layout.drop_down_list_item,
                it
            )
            (binding.addRequestOperationMenu.editText as? AutoCompleteTextView)?.setAdapter(
                operationSpinnerAdapter
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
            categorySpinnerAdapter = CategorySpinnerAdapter(
                requireContext(),
                R.layout.drop_down_list_item,
                it.filter { requestTypeArray.contains(it.type) }
            )
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
        setCommentEditTextListener()
    }

    private fun configureOperationsSpinner() {
        binding.addRequestOperationList.setOnItemClickListener { _, _, position, _ ->
            selectOperation = operationSpinnerAdapter?.getItem(position)
            selectOperation?.let {
                binding.addRequestOperationList.setText(it.name)
                binding.addRequestOperationMenu.error = null
            }
        }
    }

    private fun configureDisciplinesSpinner() {
        binding.addRequestDisciplineList.setOnItemClickListener { _, _, position, _ ->
            selectDiscipline = disciplineSpinnerAdapter?.getItem(position)
            selectDiscipline?.let {
                binding.addRequestDisciplineList.setText(it.name)
                binding.addRequestDisciplineMenu.error = null
                binding.addRequestDisciplineMenu.refreshDrawableState()
            }
        }
    }

    private fun configurePrioritySpinner() {
        binding.addRequestPriorityList.setOnItemClickListener { _, _, position, _ ->
            selectPriority = prioritySpinnerAdapter?.getItem(position)
            selectPriority?.let {
                binding.addRequestPriorityList.setText(it.name)
                binding.addRequestPriorityMenu.error = null
            }
        }
    }

    private fun configureCategorySpinner() {
        binding.addRequestCategoryList.setOnItemClickListener { _, _, position, _ ->
            selectCategory = categorySpinnerAdapter?.getItem(position)
            selectCategory?.let {
                binding.newRequestTitleTextView.text = it.comment
                binding.addRequestCategoryList.setText(it.comment)
                binding.addRequestCategoryMenu.error = null
                binding.addRequestCategoryMenu.refreshDrawableState()
                binding.addRequestCommentTextInputLayout.hint = "${it.comment}. Описание"

                if (it.type == 200) {
                    isRequest = false

                    binding.addRequestPriorityMenu.visibility = View.VISIBLE
                } else {
                    isRequest = true

                    binding.addRequestPriorityMenu.visibility = View.GONE
                }
            }
        }
    }

    private fun setCommentEditTextListener() {
        binding.addRequestCommentTextInputLayout.editText?.addTextChangedListener {
            if (!binding.addRequestCommentTextInputLayout.editText?.text.isNullOrEmpty()) {
                binding.addRequestCommentTextInputLayout.error = null
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
        return if(binding.requestTypeSwitch.isChecked) {
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
        when(addRequestMode) {
            MODE_ADD_REQUEST_WITH_SCAN -> {
                binding.equipInfoTextView.visibility = View.VISIBLE
                binding.equipInfoTextView.text = equip?.equipName
            }

            MODE_ADD_REQUEST_WITHOUT_SCAN -> {
                Toast.makeText(requireContext(), getString(R.string.add_request_no_equip_message_title), Toast.LENGTH_SHORT).show()
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