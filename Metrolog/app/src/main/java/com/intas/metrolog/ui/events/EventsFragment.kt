package com.intas.metrolog.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEventsBinding
import com.intas.metrolog.ui.events.adapter.EventListAdapter
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.ui.scanner.NfcFragment

class EventsFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var eventListAdapter: EventListAdapter
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
        setupRecyclerView()
        setUI()
        initEventObserver()

        binding.eventSwipeRefreshLayout.setOnRefreshListener {
            binding.eventSwipeRefreshLayout.isRefreshing = false
            binding.eventProgressIndicator.visibility = View.VISIBLE
            mainViewModel.getEvent()
        }
    }

    private fun setupRecyclerView() {

        eventListAdapter = EventListAdapter()

        binding.eventRecyclerView.let {
            with(it) {
                adapter = eventListAdapter

                recycledViewPool.setMaxRecycledViews(0, EventListAdapter.MAX_POOL_SIZE)
            }
        }

        setupClickListener()

        binding.eventRecyclerView.addOnScrollListener( object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.eventFilterFab.visibility = View.INVISIBLE
                    binding.searchEventFab.visibility = View.INVISIBLE
                } else {
                    binding.eventFilterFab.visibility = View.VISIBLE
                    binding.searchEventFab.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupClickListener() {

        eventListAdapter.onEventClickListener = {

            val a = it
        }

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

    private fun initEventObserver() {
        eventsViewModel.eventList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                eventListAdapter.submitList(it)
                binding.eventProgressIndicator.visibility = View.GONE
                binding.eventSwipeRefreshLayout.isRefreshing = false
            }
        }

        mainViewModel.onErrorMessage.observe(viewLifecycleOwner, {
            binding.eventSwipeRefreshLayout.isRefreshing = false
        })
    }
}