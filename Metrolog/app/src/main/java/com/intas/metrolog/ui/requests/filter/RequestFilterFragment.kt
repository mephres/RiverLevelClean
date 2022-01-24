package com.intas.metrolog.ui.requests.filter

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.intas.metrolog.R
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.databinding.FragmentBottomRequestFilterBinding
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.ui.requests.RequestViewModel
import com.intas.metrolog.util.AppPreferences
import com.intas.metrolog.util.DateTimeUtil


class RequestFilterFragment : BottomSheetDialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private val binding by lazy {
        FragmentBottomRequestFilterBinding.inflate(layoutInflater)
    }

    private var dateStart: Long? = null
    private var dateEnd: Long? = null
    private lateinit var selectDateRange: TextView
    private lateinit var disciplineChipGroup: ChipGroup
    private lateinit var statusChipGroup: ChipGroup
    private lateinit var db: AppDatabase
    private var disciplineList: MutableList<DisciplineItem>? = null
    private var statusList: MutableList<RequestStatusItem>? = null
    private var requestFilter: RequestFilter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestFilter = arguments?.getParcelable(REQUEST_FILTER)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = AppDatabase.getInstance(requireContext())
        db.disciplineDao().getAllDiscipline().observe(
            viewLifecycleOwner, {
                disciplineList = it.toMutableList()

                db.requestStatusDao().getAllRequestStatus().observe(
                    viewLifecycleOwner, {
                        statusList = it.toMutableList()
                        showFilter(disciplineList, statusList)
                    }
                )
            }
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectDateRange = binding.selectRequestDateTextView
        disciplineChipGroup = binding.incidentCategoryChipGroup
        statusChipGroup = binding.requestStatusChipGroup

        binding.requestDateRangeClearImageView.setOnClickListener {
            dateStart = 0
            dateEnd = 0
            selectDateRange.text = getString(R.string.no_chosen_filter_title)
            binding.requestDateRangeClearImageView.visibility = ViewGroup.GONE
        }

        binding.selectRequestDateTextView.setOnClickListener {
            showCalendar()
        }

        binding.requestFilterApplyButton.setOnClickListener {
            createFilter()
            closeFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    private fun showCalendar() {
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Выберите период")
                .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .setSelection(
                    Pair(
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    )
                ).build()

        dateRangePicker.addOnPositiveButtonClickListener {
            dateStart = it.first.div(1000)
            dateEnd = (it.second.plus(DateTimeUtil.DAY_IN_MILLIS)).div(1000)

            val dateRangeTemplate = getString(R.string.date_range_placeholder)

            selectDateRange.text = String.format(
                dateRangeTemplate,
                DateTimeUtil.getShortDataFromMili(it.first.div(1000)),
                DateTimeUtil.getShortDataFromMili(it.second.div(1000)))
            binding.requestDateRangeClearImageView.visibility = ViewGroup.VISIBLE
        }
        dateRangePicker.show(
            requireActivity().supportFragmentManager,
            REQUEST_DATE_RANGE_PICKER_TAG
        )
    }

    private fun showFilter(
        disciplineList: List<DisciplineItem>?,
        statusList: List<RequestStatusItem>?
    ) {
        requestFilter?.let {
            if (it.dateStart > 0 && it.dateEnd > 0) {

                val dateRangeTemplate = getString(R.string.date_range_placeholder)

                selectDateRange.text = String.format(
                    dateRangeTemplate,
                    DateTimeUtil.getShortDataFromMili(it.dateStart),
                    DateTimeUtil.getShortDataFromMili(it.dateEnd.minus(DateTimeUtil.DAY_IN_SECONDS)))
                binding.requestDateRangeClearImageView.visibility = ViewGroup.VISIBLE
            }

            disciplineChipGroup.removeAllViews()
            disciplineList?.let {
                for (discipline in disciplineList) {
                    discipline.let {
                        val chip = addChip(
                            requireContext(),
                            disciplineChipGroup,
                            it.name,
                            it.id
                        )
                        if (requestFilter!!.requestDisciplineIdList.contains(it.id)) {
                            chip.isChecked = true
                        }
                    }
                }
            }

            statusChipGroup.removeAllViews()
            statusList?.let {
                for (status in statusList) {
                    status.let {
                        val chip = addChip(
                            requireContext(),
                            statusChipGroup,
                            it.name,
                            it.id
                        )
                        if (requestFilter!!.requestStatusIdList.contains(it.id)) {
                            chip.isChecked = true
                        }
                    }
                }
            }
        }
    }

    private fun addChip(
        context: Context,
        chipGroup: ChipGroup,
        chipText: String,
        chipTag: Int
    ): Chip {
        val chip = Chip(context)
        chip.isCheckable = true
        chip.text = chipText
        chip.tag = chipTag
        chip.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
        chip.setTextColor(ContextCompat.getColor(context, R.color.md_white))
        chip.checkedIconTint =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_white))
        chip.isChecked = false

        chipGroup.addView(chip)

        return chip
    }

    private fun createFilter() {
        val disciplineChipCount = disciplineChipGroup.childCount
        val disciplineIdArray = mutableListOf<Int>()
        for (i in 0 until disciplineChipCount) {
            val chip = disciplineChipGroup.getChildAt(i) as Chip
            val disciplineId = chip.tag as Int
            if (chip.isChecked) {
                disciplineIdArray.add(disciplineId)
            } else {
                requestFilter?.requestDisciplineIdList?.remove(disciplineId)
            }
        }
        requestFilter?.requestDisciplineIdList = disciplineIdArray as ArrayList<Int>

        val statusChipCount = statusChipGroup.childCount
        val statusIdArray = mutableListOf<Int>()
        for (i in 0 until statusChipCount) {
            val chip = statusChipGroup.getChildAt(i) as Chip
            val statusId = chip.tag as Int
            if (chip.isChecked) {
                statusIdArray.add(statusId)
            }
        }
        requestFilter?.requestStatusIdList = statusIdArray as ArrayList<Int>


        requestFilter?.dateStart = (dateStart ?: requestFilter?.dateStart) as Long
        requestFilter?.dateEnd = (dateEnd ?: requestFilter?.dateEnd) as Long
        requestFilter?.let {
            AppPreferences.requestFilterDiscList = it.requestDisciplineIdList
            AppPreferences.requestFilterStatusList = it.requestStatusIdList
            AppPreferences.requestFilterDateStart = it.dateStart
            AppPreferences.requestFilterDateEnd = it.dateEnd
            onAddFilter(it)
        }
    }

    private fun onAddFilter(requestFilter: RequestFilter) {
        viewModel.addRequestFilter(requestFilter)
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(REQUEST_FILTER_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    companion object {
        private const val REQUEST_FILTER = "request_filter"
        const val REQUEST_FILTER_TAG = "request_filter_tag"

        const val REQUEST_DATE_RANGE_PICKER_TAG = "requestDateRangePickerTag"
        const val REQUEST_DATE_START = "requestDateStart"
        const val REQUEST_DATE_END = "requestDateEnd"

        fun newInstance(filter: RequestFilter) = RequestFilterFragment().apply {
            arguments = Bundle().apply {
                putParcelable(REQUEST_FILTER, filter)
            }
        }
    }
}