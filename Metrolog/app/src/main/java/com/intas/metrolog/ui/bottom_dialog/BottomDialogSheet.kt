package com.intas.metrolog.ui.bottom_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.databinding.FragmentBottomDialogSheetBinding
import com.intas.metrolog.util.Util.BOTTOM_DIALOG_SHEET_FRAGMENT_TAG

private const val DIALOG_TITLE = "dialogTitle"
private const val DIALOG_TEXT = "dialogText"
private const val DIALOG_POSITIVE_BUTTON_TEXT = "dialogPositiveButtonText"
private const val DIALOG_NEGATIVE_BUTTON_TEXT = "dialogNegativeButtonText"

class BottomDialogSheet : BottomSheetDialogFragment() {

    private val binding by lazy {
        FragmentBottomDialogSheetBinding.inflate(layoutInflater)
    }

    var onPositiveClickListener: ((View) -> Unit)? = null
    var onNegativeClickListener: ((View) -> Unit)? = null


    private var dialogTitle: String? = null
    private var dialogText: String? = null
    private var dialogPositiveButtonText: String? = null
    private var dialogNegativeButtonText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogTitle = it.getString(DIALOG_TITLE)
            dialogText = it.getString(DIALOG_TEXT)
            dialogPositiveButtonText = it.getString(DIALOG_POSITIVE_BUTTON_TEXT)
            dialogNegativeButtonText = it.getString(DIALOG_NEGATIVE_BUTTON_TEXT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            dialogSheetTitleTextView.text = dialogTitle
            dialogSheetTextTextView.text = dialogText
            dialogSheetPositiveButton.text = dialogPositiveButtonText
            dialogSheetNegativeButton.text = dialogNegativeButtonText
        }
        binding.dialogSheetPositiveButton.setOnClickListener {
            onPositiveClickListener?.invoke(it)
            closeDialog()
        }

        binding.dialogSheetNegativeButton.setOnClickListener {
            onNegativeClickListener?.invoke(it)
            closeDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private fun closeDialog() {

        val dialogSheetFragment =
            parentFragmentManager.findFragmentByTag(BOTTOM_DIALOG_SHEET_FRAGMENT_TAG)
        dialogSheetFragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(
            title: String,
            text: String,
            positiveButtonTitle: String,
            negativeButtonTitle: String
        ) =
            BottomDialogSheet().apply {
                arguments = Bundle().apply {
                    putString(DIALOG_TITLE, title)
                    putString(DIALOG_TEXT, text)
                    putString(DIALOG_POSITIVE_BUTTON_TEXT, positiveButtonTitle)
                    putString(DIALOG_NEGATIVE_BUTTON_TEXT, negativeButtonTitle)
                }
            }
    }
}