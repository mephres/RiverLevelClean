package com.intas.metrolog.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {

    private val binding by lazy {
        FragmentEventsBinding.inflate(layoutInflater)
    }

    private lateinit var eventsViewModel: EventsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventsViewModel = ViewModelProvider(this)[EventsViewModel::class.java]
        return binding.root
    }

}