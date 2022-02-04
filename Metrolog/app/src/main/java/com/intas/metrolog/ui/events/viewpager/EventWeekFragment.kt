package com.intas.metrolog.ui.events.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.databinding.FragmentEventTodayBinding
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.ui.events.EventsViewModel
import com.intas.metrolog.ui.events.adapter.EventListAdapter
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.ui.operation.OperationActivity
import com.intas.metrolog.util.Journal

class EventWeekFragment : Fragment() {

    private lateinit var eventListAdapter: EventListAdapter
    private val eventViewModel: EventsViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var eventList = mutableListOf<EventItem>()

    private val binding by lazy {
        FragmentEventTodayBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        eventViewModel.getEventListWeek().observe(viewLifecycleOwner, {
            eventListAdapter.submitList(it)
            eventList = it.toMutableList()
            Journal.insertJournal("EventWeekFragment->eventList", list = eventList)
        })

        binding.fragmentEventSwipeRefreshLayout.setOnRefreshListener {
            binding.fragmentEventSwipeRefreshLayout.isRefreshing = true
            mainViewModel.getEvent()
            binding.fragmentEventSwipeRefreshLayout.isRefreshing = false
            Journal.insertJournal("EventWeekFragment->fragmentEventSwipeRefreshLayout", "isRefreshing")
        }


        eventViewModel.searchText.observe(viewLifecycleOwner, {
            setFilter(it)
        })
    }

    private fun setFilter(text: String) {
        if (text.isEmpty()) {
            eventListAdapter.submitList(eventList)
            return
        }
        if (eventList.isNullOrEmpty()) {
            return
        }

        eventListAdapter.submitList(eventList.filter {
            it.name?.contains(text, true) == true || it.equipName?.trim()?.contains(text, true) == true
        })
    }

    override fun onDetach() {
        super.onDetach()
    }

    fun setupRecyclerView() {

        val eventRecyclerView = binding.eventRecyclerView
        eventListAdapter = EventListAdapter()
        eventRecyclerView?.let {
            with(it) {
                adapter = eventListAdapter

                recycledViewPool.setMaxRecycledViews(0, EventListAdapter.MAX_POOL_SIZE)
            }
        }

        setupClickListener()
        onScrollListener()
    }

    private fun setupClickListener() {

        eventListAdapter.onEventClickListener = {
            Journal.insertJournal("EventWeekFragment->onEventClickListener", it)
            startActivity(OperationActivity.newIntent(requireContext(), it.opId, true))
        }
    }

    private fun onScrollListener() {
        binding.eventRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                eventViewModel.onScrolled(dy)
            }
        })
    }
}