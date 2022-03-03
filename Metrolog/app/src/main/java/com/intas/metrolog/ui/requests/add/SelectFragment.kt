package com.intas.metrolog.ui.requests.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.databinding.FragmentBottomSelectBinding

class SelectFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSelectBinding? = null
    private val binding get() = _binding!!

    var onSelectScannerClickListener: ((View) -> Unit)? = null
    var onSelectWithoutScannerClickListener: ((View) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectScannerContainer.setOnClickListener {
            onSelectScannerClickListener?.invoke(it)
            closeFragment()
        }
        binding.withoutScannerContainer.setOnClickListener {
            onSelectWithoutScannerClickListener?.invoke(it)
            closeFragment()
        }
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(SELECT_FRAGMENT_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val SELECT_FRAGMENT_TAG = "select_fragment_tag"
        fun newInstance() =
            SelectFragment().apply {

            }
    }
}