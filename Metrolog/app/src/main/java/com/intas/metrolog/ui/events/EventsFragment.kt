package com.intas.metrolog.ui.events

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEventsBinding
import com.intas.metrolog.ui.events.viewpager.*
import com.intas.metrolog.ui.events.viewpager.adapter.ViewPagerAdapter
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.ui.operation.OperationActivity
import com.intas.metrolog.ui.scanner.NfcFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class EventsFragment : Fragment(R.layout.fragment_events) {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val eventsViewModel: EventsViewModel by activityViewModels()
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var searchView: SearchView? = null

    private val binding by viewBinding(FragmentEventsBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
        viewPagerAdapter = childFragmentManager.let { ViewPagerAdapter(it) }

        val fragmentList: List<Fragment> =
            parentFragmentManager.getFragments() as List<Fragment>

        viewPagerAdapter.addFragment(EventTodayFragment(), "Сегодня", 0)
        viewPagerAdapter.addFragment(EventWeekFragment(), "Неделя", 1)
        viewPagerAdapter.addFragment(EventMonthFragment(), "Месяц", 2)
        viewPagerAdapter.addFragment(EventCompletedFragment(), "Выполненные", 3)
        viewPagerAdapter.addFragment(EventCanceledFragment(), "Отмененные", 4)

        binding.eventViewPager.adapter = viewPagerAdapter
        binding.eventViewPager.offscreenPageLimit = 3
        binding.eventTabLayout.setupWithViewPager(binding.eventViewPager)

        viewPagerAdapter.notifyDataSetChanged()

        setupClickListener()
        setSearchViewListener()

        eventsViewModel.scroll.observe(viewLifecycleOwner, {
            if (it > 0) {
                binding.searchEventFab.hide()
            } else {
                binding.searchEventFab.show()
            }
        })

    }


    private fun setupClickListener() {

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

    private fun setSearchViewListener() {
        searchView?.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            val handler = Handler(Looper.getMainLooper())
            override fun onQueryTextChange(newText: String): Boolean {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    setFilter(newText.trim())
                }, 300)
                return true
            }
        })
    }

    private fun setFilter(text: String) {
        eventsViewModel.setSearchText(text)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        try {
            eraseEventFragmentList()
        } finally {
        }
    }

    /**
     * Удаление фрагментов для отображения списка за день, неделю и месяц из стэка
     */
    private fun eraseEventFragmentList() {
        try {
            for (i in 0 until viewPagerAdapter.count) {
                val eventFragmentTag = "android:switcher:${R.id.eventViewPager}:$i"
                val findEventFragment =
                    parentFragmentManager.findFragmentByTag(eventFragmentTag)
                findEventFragment?.let {
                    parentFragmentManager.beginTransaction().remove(it).commit()
                }
            }
        } catch (e: Exception) {

        }
    }
}