package com.intas.metrolog.ui.events.select_event

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.databinding.FragmentBottomSelectEventBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.equip.adapter.EquipListAdapter
import com.intas.metrolog.ui.events.select_event.adapter.SelectEventListAdapter
import com.intas.metrolog.ui.operation.OperationActivity

class SelectEventFragment : BottomSheetDialogFragment() {
    private lateinit var selectEventListAdapter: SelectEventListAdapter
    private var equipItem: EquipItem? = null
    private var equipRfid: String = ""

    private val binding by lazy {
        FragmentBottomSelectEventBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[SelectEventViewModel::class.java]
    }

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

        binding.equipInfoTextView.text = equipItem?.equipName

        viewModel.getEventByRfid(equipRfid).observe(viewLifecycleOwner, {
            binding.eventNotFoundTextView.isVisible = it.isEmpty()
            selectEventListAdapter.submitList(it)
        })

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun parseArgs() {
        equipItem = requireArguments().getParcelable(EQUIP_ITEM)
        if (equipItem == null) return

        equipRfid = equipItem?.equipRFID.toString()
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
            startActivity(OperationActivity.newIntent(requireContext(), it.opId))
        }
    }

    companion object {
        const val SELECT_EVENT_FRAGMENT = "select_event_fragment"
        private const val EQUIP_ITEM = "equip_item"

        fun newInstance(equip: EquipItem) =
            SelectEventFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EQUIP_ITEM, equip)
                }
            }
    }
}