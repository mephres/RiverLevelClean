package com.intas.metrolog.ui.operation.equip_info

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.intas.metrolog.databinding.FragmentEquipInfoBinding
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.operation.equip_info.adapter.EquipInfoListAdapter
import com.intas.metrolog.util.Journal

class EquipInfoFragment : BottomSheetDialogFragment() {
    private lateinit var equipInfoListAdapter: EquipInfoListAdapter

    private val binding by lazy {
        FragmentEquipInfoBinding.inflate(layoutInflater)
    }

    private val viewModel: EquipInfoViewModel by viewModels()
    private var equipItem: EquipItem? = null
    private var equipId: Long = 0
    private val checkedEquipInfoList: MutableList<EquipInfo> = mutableListOf()

    var onCheckedListIsEmpty: ((View) -> Unit)? = null

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setClickListeners()

        binding.equipInfoNameTextView.text = equipItem?.equipName

        viewModel.getEquipInfoById(equipId).observe(viewLifecycleOwner) {
            equipInfoListAdapter.submitList(it)
        }
    }

    private fun setupRecyclerView() {
        equipInfoListAdapter = EquipInfoListAdapter()
        with(binding.equipInfoRecyclerView) {
            adapter = equipInfoListAdapter
            recycledViewPool.setMaxRecycledViews(0, EquipInfoListAdapter.MAX_POOL_SIZE)
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun setClickListeners() {
        equipInfoListAdapter.onEquipInfoItemCheckedListener = { info ->
            checkedEquipInfoList.add(info)
            Journal.insertJournal("EquipInfoFragment->onEquipInfoChecked", checkedEquipInfoList.toString())
        }

        equipInfoListAdapter.onEquipInfoItemUncheckedListener = { info ->
            checkedEquipInfoList.removeIf { it.id == info.id }
            Journal.insertJournal("EquipInfoFragment->onEquipInfoUnchecked", checkedEquipInfoList.toString())
        }

        binding.equipInfoCheckedConfirmButton.setOnClickListener {
            if (checkedEquipInfoList.isNotEmpty()) {
                viewModel.updateEquipInfo(checkedEquipInfoList)
                closeFragment()
            } else {
                showIsInfoNotCheckedMessage()
            }
            Journal.insertJournal("EquipInfoFragment->equipInfoCheckedConfirmButton", checkedEquipInfoList.toString())
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(EQUIP_ITEM)) {
            return
        }
        equipItem = args.getParcelable(EQUIP_ITEM)
        equipId = equipItem?.equipId ?: 0

        if (equipItem == null) {
            return
        }

        Journal.insertJournal("EquipInfoFragment->parseArgs", equipItem.toString())
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(EQUIP_INFO_FRAGMENT_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun showIsInfoNotCheckedMessage() {
        val snackbar =
            Snackbar.make(requireView(), "Для подтверждения необходимо отметить элемент списка", Snackbar.LENGTH_LONG)
        snackbar.anchorView = binding.root
        snackbar.show()
    }

    companion object {
        const val EQUIP_INFO_FRAGMENT_TAG = "equip_info_fragment_tag"

        private const val EQUIP_ITEM = "equip_item"

        fun newInstance(equipItem: EquipItem) = EquipInfoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EQUIP_ITEM, equipItem)
            }
        }
    }
}