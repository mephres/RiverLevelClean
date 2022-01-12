package com.intas.metrolog.ui.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.databinding.FragmentRequestsBinding

class RequestsFragment : Fragment() {

    private val binding by lazy {
        FragmentRequestsBinding.inflate(layoutInflater)
    }

    private lateinit var requestViewModel: RequestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requestViewModel = ViewModelProvider(this)[RequestViewModel::class.java]
        return binding.root
    }

}