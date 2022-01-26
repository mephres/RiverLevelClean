package com.intas.metrolog.ui.events

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentBottomSelectEventBinding

class SelectEventFragment : BottomSheetDialogFragment() {
    private var rfid: String? = null

    private val binding by lazy {
        FragmentBottomSelectEventBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        const val SELECT_EVENT_FRAGMENT = "select_event_fragment"
        private const val RFID = "rfid"

        fun newInstance(rfid: String) =
            SelectEventFragment().apply {
                arguments = Bundle().apply {
                    putString(RFID, rfid)
                }
            }
    }
}