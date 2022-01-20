package com.intas.metrolog.ui.requests.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.databinding.FragmentBottomAddRequestBinding

class AddRequestFragment : BottomSheetDialogFragment() {
    private var requestImageFabVisible = false

    private val binding by lazy {
        FragmentBottomAddRequestBinding.inflate(layoutInflater)
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

        binding.addRequestImageFab.setOnClickListener {
            showRequestImageFab()
        }

        binding.addRequestImageGalleryFab.setOnClickListener {

        }

        binding.addRequestImageCameraFab.setOnClickListener {

        }
    }

    private fun showRequestImageFab() {

        requestImageFabVisible = !requestImageFabVisible

        binding.addRequestImageGalleryFab.visibility = View.INVISIBLE
        binding.addRequestImageCameraFab.visibility = View.INVISIBLE

        if (requestImageFabVisible) {
            binding.addRequestImageGalleryFab.visibility = View.VISIBLE
            binding.addRequestImageCameraFab.visibility = View.VISIBLE
        }
    }
}