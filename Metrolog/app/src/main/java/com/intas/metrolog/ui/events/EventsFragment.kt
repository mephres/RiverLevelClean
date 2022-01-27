package com.intas.metrolog.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEventsBinding
import com.intas.metrolog.ui.scanner.NfcFragment

class EventsFragment : Fragment() {
    private var searchView: SearchView? = null

    private val binding by lazy {
        FragmentEventsBinding.inflate(layoutInflater)
    }

    private lateinit var eventsViewModel: EventsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventsViewModel = ViewModelProvider(this)[EventsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()

        binding.searchEventFab.setOnClickListener {
            showScanner()
        }
    }

    private fun showScanner() {
        val scanner = NfcFragment.newInstanceGetEvent()
        scanner.show(requireActivity().supportFragmentManager, NfcFragment.NFC_FRAGMENT_TAG)
    }

    private fun setUI() {
        binding.include.toolbar.title = "Мероприятия"

        val menu = binding.include.toolbar.menu
        val menuItemSearch = menu?.findItem(R.id.action_search)

        searchView = menuItemSearch?.actionView as SearchView
        searchView?.queryHint = "Поиск мероприятий"
    }

}