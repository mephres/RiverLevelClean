package com.intas.metrolog.ui.events.select_event

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.databinding.FragmentBottomSelectEventBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.events.select_event.adapter.SelectEventListAdapter
import com.intas.metrolog.ui.operation.OperationActivity

class SelectEventFragment : BottomSheetDialogFragment() {
    private lateinit var selectEventListAdapter: SelectEventListAdapter
    private var equipItem: EquipItem? = null
    private var equipRfid: String = ""
    private var scannerMode: String = MODE_UNKNOWN
    private var needVerify: Boolean = false

    private val binding by lazy {
        FragmentBottomSelectEventBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[SelectEventViewModel::class.java]
    }

    private val modes = arrayOf(MODE_GET_EVENTS_BY_EQUIP, MODE_GET_HIGH_PRIORITY_EVENTS,
        MODE_GET_LAUNCHED_HIGH_PRIORITY_EVENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        launchMode()

        dialog?.setOnDismissListener {
            if (requireActivity() is OperationActivity) requireActivity().finish()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(SCANNER_MODE)) {
            return
        }
        val mode = args.getString(SCANNER_MODE)
        if (!modes.contains(mode)) {
            return
        }
        mode?.let { scannerMode = it }

        if (scannerMode == MODE_GET_EVENTS_BY_EQUIP) {
            if (!args.containsKey(EQUIP_ITEM)) {
                return
            }
            equipItem = args.getParcelable(EQUIP_ITEM)

            if (equipItem == null) {
                return
            }
            equipRfid = equipItem?.equipRFID.toString()
        }
    }

    private fun launchMode() {
        when (scannerMode) {
            MODE_GET_EVENTS_BY_EQUIP -> {

                binding.equipInfoTextView.text = equipItem?.equipName

                val eventList = viewModel.getEventByRfid(equipRfid)
                if (eventList.isNotEmpty()) {
                    selectEventListAdapter.submitList(eventList)
                }
            }
            MODE_GET_HIGH_PRIORITY_EVENTS -> {

                needVerify = true

                val eventList = viewModel.getHighPriorityEventList()
                if (eventList.isNotEmpty()) {
                    selectEventListAdapter.submitList(eventList)
                }
            }
            MODE_GET_LAUNCHED_HIGH_PRIORITY_EVENT -> {

                binding.selectEventTitleTextView.visibility = View.GONE
                binding.eventNotCompletedView.visibility = View.VISIBLE
                needVerify = true

                val eventList = viewModel.getLaunchedHighPriorityEvent()
                if (eventList.isNotEmpty()) {
                    selectEventListAdapter.submitList(eventList)
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setRecyclerView() {
        selectEventListAdapter = SelectEventListAdapter()

        with(binding.selectEventRecyclerView) {
            adapter = selectEventListAdapter
            recycledViewPool.setMaxRecycledViews(0, SelectEventListAdapter.MAX_POOL_SIZE)
        }
        setClickListener()
    }

    private fun setClickListener() {
        selectEventListAdapter.onItemClickListener = {
            if (requireActivity() is OperationActivity) requireActivity().finish()

            startActivity(OperationActivity.newIntent(requireContext(), it.opId, needVerify))
            closeFragment()
        }
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(SELECT_EVENT_FRAGMENT)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    companion object {
        const val SELECT_EVENT_FRAGMENT = "select_event_fragment"

        private const val SCANNER_MODE = "scanner_mode"
        private const val MODE_UNKNOWN = "unknown_mode"
        private const val MODE_GET_EVENTS_BY_EQUIP = "mode_get_events_by_equip"
        private const val MODE_GET_HIGH_PRIORITY_EVENTS = "mode_get_high_priority_events"
        private const val MODE_GET_LAUNCHED_HIGH_PRIORITY_EVENT = "mode_get_launched_high_priority_event"
        private const val EQUIP_ITEM = "equip_item"

        fun newInstance(equip: EquipItem) =
            SelectEventFragment().apply {
                arguments = Bundle().apply {
                    putString(SCANNER_MODE, MODE_GET_EVENTS_BY_EQUIP)
                    putParcelable(EQUIP_ITEM, equip)
                }
            }

        fun newInstanceGetHighPriorityEvents() =
            SelectEventFragment().apply {
                arguments = Bundle().apply {
                    putString(SCANNER_MODE, MODE_GET_HIGH_PRIORITY_EVENTS)
                }
            }

        fun newInstanceGetLaunchedHighPriorityEvent() =
            SelectEventFragment().apply {
                arguments = Bundle().apply {
                    putString(SCANNER_MODE, MODE_GET_LAUNCHED_HIGH_PRIORITY_EVENT)
                }
            }
    }
}