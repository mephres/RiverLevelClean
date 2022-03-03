package com.intas.metrolog.ui.requests

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEquipBinding
import com.intas.metrolog.databinding.FragmentRequestsBinding
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.ui.requests.adapter.RequestListAdapter
import com.intas.metrolog.ui.requests.add.AddRequestFragment
import com.intas.metrolog.ui.requests.add.SelectFragment
import com.intas.metrolog.ui.requests.filter.RequestFilter
import com.intas.metrolog.ui.requests.filter.RequestFilterFragment
import com.intas.metrolog.ui.scanner.NfcFragment
import com.intas.metrolog.util.AppPreferences
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class RequestsFragment : Fragment(R.layout.fragment_requests) {
    private lateinit var requestListAdapter: RequestListAdapter
    private var searchView: SearchView? = null
    private var requestList = mutableListOf<RequestItem>()
    private var requestFilter: RequestFilter? = null

    private val binding by viewBinding(FragmentRequestsBinding::bind)

    private val requestViewModel by lazy {
        ViewModelProvider(this)[RequestViewModel::class.java]
    }

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        setUI()
        initObserver()
        setSearchViewListener()
        onScrollListener()

        binding.requestSwipeRefreshLayout.setOnRefreshListener {
            binding.requestSwipeRefreshLayout.isRefreshing = false
            binding.requestProgressIndicator.visibility = View.VISIBLE
            mainViewModel.getRequestList()
        }

        binding.addNewRequestFab.setOnClickListener {
            showSelectDialog()
        }

        binding.requestFilterFab.setOnClickListener {
            showFilter()
        }

        requestViewModel.scroll.observe(viewLifecycleOwner, {
            if (it > 0) {
                binding.addNewRequestFab.hide()
                binding.requestFilterFab.hide()
            } else {
                binding.addNewRequestFab.show()
                binding.requestFilterFab.show()
            }
        })
    }

    private fun showSelectDialog() {
        val fragment = SelectFragment.newInstance()
        fragment.show(requireActivity().supportFragmentManager, SelectFragment.SELECT_FRAGMENT_TAG)
        fragment.onSelectScannerClickListener = {
            showScanner()
        }
        fragment.onSelectWithoutScannerClickListener = {
            val addRequestFragment = AddRequestFragment.newInstanceWithoutRfid()
            addRequestFragment.show(
                requireActivity().supportFragmentManager,
                AddRequestFragment.ADD_REQUEST_FRAGMENT_TAG
            )
        }
    }

    private fun showScanner() {
        val scanner = NfcFragment.newInstanceAddRequest()
        scanner.show(requireActivity().supportFragmentManager, NfcFragment.NFC_FRAGMENT_TAG)
    }

    private fun setUI() {
        binding.include.toolbar.title = getString(R.string.bottom_menu_requests_title)

        val menu = binding.include.toolbar.menu
        val menuItemSearch = menu?.findItem(R.id.action_search)

        searchView = menuItemSearch?.actionView as SearchView
        searchView?.queryHint = getString(R.string.request_search_bar_hint)
    }

    private fun initObserver() {
        lifecycleScope.launchWhenResumed {
            requestViewModel.requestList.observe(viewLifecycleOwner, {
                if (it.isNotEmpty()) {
                    binding.requestProgressIndicator.visibility = View.GONE
                    binding.requestSwipeRefreshLayout.isRefreshing = false
                    requestList = it.toMutableList()
                    requestListAdapter.submitList(it.filter {
                        if (requestFilter != null) {
                            var dateStart: Long? = null
                            var dateEnd: Long? = null
                            if (requestFilter?.dateStart != 0L) dateStart = requestFilter?.dateStart
                            if (requestFilter?.dateEnd != 0L) dateEnd = requestFilter?.dateEnd

                            val a =
                                requestFilter?.requestDisciplineIdList?.contains(it.discipline) == true &&
                                        requestFilter?.requestStatusIdList?.contains(it.status) == true &&
                                        dateStart ?: it.creationDate <= it.creationDate &&
                                        dateEnd ?: it.creationDate >= it.creationDate
                            a
                        } else {
                            true
                        }
                    })
                }
            })

            mainViewModel.requestFilter.observe(viewLifecycleOwner, {

                requestFilter = it
                var dateStart: Long? = null
                var dateEnd: Long? = null
                if (requestFilter?.dateStart != 0L) dateStart = requestFilter?.dateStart
                if (requestFilter?.dateEnd != 0L) dateEnd = requestFilter?.dateEnd

                val result = requestList.filter {
                    requestFilter?.requestDisciplineIdList?.contains(it.discipline) == true &&
                            requestFilter?.requestStatusIdList?.contains(it.status) == true &&
                            dateStart ?: it.creationDate <= it.creationDate &&
                            dateEnd ?: it.creationDate >= it.creationDate
                }

                result.let {
                    requestListAdapter.submitList(it)
                }
            })
        }
    }

    private fun showFilter() {
        try {
            val requestFilterDiscList = AppPreferences.requestFilterDiscList
            val requestFilterStatusList = AppPreferences.requestFilterStatusList
            val dateStart = AppPreferences.requestFilterDateStart
            val dateEnd = AppPreferences.requestFilterDateEnd

            val requestFilter = RequestFilter(
                dateStart = dateStart ?: 0,
                dateEnd = dateEnd ?: 0,
                requestDisciplineIdList = requestFilterDiscList,
                requestStatusIdList = requestFilterStatusList
            )
            val filterFragment = RequestFilterFragment.newInstance(requestFilter)
            filterFragment.show(
                requireActivity().supportFragmentManager,
                RequestFilterFragment.REQUEST_FILTER_TAG
            )
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            showToast("При загрузке списка дисциплин и статусов заявки возникла ошибка")
        }
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
        if (text.isEmpty()) {
            requestListAdapter.submitList(requestList)
            return
        }
        if (requestList.isNullOrEmpty()) {
            return
        }

        requestListAdapter.submitList(requestList.filter {
            it.equipInfo?.trim()?.contains(text, true) == true
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showSnackBar(message: String) {
        val snackbar =
            Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("OK") {
            snackbar.dismiss()
        }
        val messageView = snackbar.view.findViewById(R.id.snackbar_text) as TextView
        messageView.maxLines = 20
        snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.md_white))
        snackbar.show()
    }

    private fun setRecyclerView() {
        requestListAdapter = RequestListAdapter()

        with(binding.requestRecyclerView) {
            adapter = requestListAdapter
            itemAnimator = null
            recycledViewPool.setMaxRecycledViews(0, RequestListAdapter.MAX_POOL_SIZE)
        }
        setClickListener()
    }

    private fun setClickListener() {
        requestListAdapter.onRequestLongClickListener = {

        }
    }

    private fun onScrollListener() {
        binding.requestRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                requestViewModel.onScrolled(dy)
            }
        })
    }
}