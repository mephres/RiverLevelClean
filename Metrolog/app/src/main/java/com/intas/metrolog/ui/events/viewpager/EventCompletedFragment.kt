package com.intas.metrolog.ui.events.viewpager

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEventTodayBinding
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.ui.bottom_dialog.BottomDialogSheet
import com.intas.metrolog.ui.events.EventsViewModel
import com.intas.metrolog.ui.events.adapter.EventListAdapter
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.ui.operation.OperationActivity
import com.intas.metrolog.util.AppPreferences
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.launch


class EventCompletedFragment : Fragment(R.layout.fragment_event_today) {
    private lateinit var eventListAdapter: EventListAdapter
    private val eventViewModel: EventsViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var eventList = mutableListOf<EventItem>()


    private val binding by viewBinding(FragmentEventTodayBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.eventProgressIndicator.visibility = View.GONE

        lifecycleScope.launch {
            eventViewModel.getEventListCompleted().observe(viewLifecycleOwner, {
                eventListAdapter.submitList(it)
                eventList = it.toMutableList()
                Journal.insertJournal("EventCompletedFragment->eventList", list = eventList)
            })

            eventViewModel.searchText.observe(viewLifecycleOwner, {
                setFilter(it)
            })

            mainViewModel.equipLoaded.observe(viewLifecycleOwner, {
                if (it) {
                    eventListAdapter.notifyDataSetChanged()
                }
            })
        }

        binding.fragmentEventSwipeRefreshLayout.setOnRefreshListener {
            binding.fragmentEventSwipeRefreshLayout.isRefreshing = true
            eventListAdapter.submitList(eventList)
            binding.fragmentEventSwipeRefreshLayout.isRefreshing = false
            Journal.insertJournal(
                "EventCompletedFragment->fragmentEventSwipeRefreshLayout",
                "isRefreshing"
            )
        }
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
                itemAnimator = null
                recycledViewPool.setMaxRecycledViews(0, EventListAdapter.MAX_POOL_SIZE)
            }
        }

        setupClickListener()
        onScrollListener()
    }

    private fun setupClickListener() {

        eventListAdapter.onEventClickListener = {
            Journal.insertJournal("EventCompletedFragment->onEventClickListener", it)
            startActivity(OperationActivity.newIntent(requireContext(), it.opId, true))
        }

        eventListAdapter.onEventLongClickListener = {
            if(it.isSended == 1) deleteEventDialog(it.opId)
        }
    }

    private fun deleteEventDialog(eventId: Long) {
        val dialogSheet = BottomDialogSheet.newInstance(
            getString(R.string.event_delete_dialog_title),
            getString(R.string.event_delete_dialog_dialog_text),
            getString(R.string.equip_document_activity_delete_image_dialog_positive_button),
            getString(R.string.pequip_document_activity_delete_image_dialog_negative_button)
        )
        dialogSheet.show(childFragmentManager, Util.BOTTOM_DIALOG_SHEET_FRAGMENT_TAG)
        dialogSheet.onPositiveClickListener = {
            eventViewModel.deleteEventById(eventId)
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