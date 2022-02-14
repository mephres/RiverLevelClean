package com.intas.metrolog.ui.equip.equip_menu

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.databinding.FragmentEquipMenuBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.util.Journal

class EquipMenuFragment : BottomSheetDialogFragment() {
    var onAddRFIDClickListener: ((EquipItem) -> Unit)? = null
    var onCreateDocumentClickListener: ((EquipItem) -> Unit)? = null

    private val binding by lazy {
        FragmentEquipMenuBinding.inflate(layoutInflater)
    }

    private var equipItem: EquipItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
        binding.equipNameTitleTextView.text = equipItem?.equipName
    }

    private fun setClickListeners() {
        equipItem?.let { equip ->
            binding.addRFIDView.setOnClickListener {
                onAddRFIDClickListener?.invoke(equip)
                Journal.insertJournal("EquipMenuFragment->onAddRFID", equipItem.toString())
                closeFragment()
            }

            binding.createDocumentView.setOnClickListener {
                onCreateDocumentClickListener?.invoke(equip)
                Journal.insertJournal("EquipMenuFragment->onCreateDocument", equipItem.toString())
                closeFragment()
            }
        }
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(EQUIP_MENU_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(EQUIP_MENU_ITEM)) {
            return
        }
        equipItem = args.getParcelable(EQUIP_MENU_ITEM)

        Journal.insertJournal("EquipMenuFragment->parseArgs", equipItem.toString())
    }


    companion object {
        const val EQUIP_MENU_TAG = "equip_menu_tag"

        private const val EQUIP_MENU_ITEM = "equip_menu_item"

        fun newInstance(equip: EquipItem?) = EquipMenuFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EQUIP_MENU_ITEM, equip)
            }
        }
    }
}