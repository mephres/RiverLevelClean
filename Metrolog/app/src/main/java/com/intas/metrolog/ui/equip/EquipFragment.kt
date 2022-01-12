package com.intas.metrolog.ui.equip

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEquipBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.equip.adapter.EquipListAdapter
import com.intas.metrolog.ui.main.MainViewModel

class EquipFragment : Fragment() {
    private lateinit var equipListAdapter: EquipListAdapter
    private var searchView: SearchView? = null
    private var equipList = mutableListOf<EquipItem>()

    private val binding by lazy {
        FragmentEquipBinding.inflate(layoutInflater)
    }
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var equipViewModel: EquipViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        equipViewModel = ViewModelProvider(this)[EquipViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        setUI()
        initObserver()
        setSearchViewListener()

        binding.equipSwipeRefreshLayout.setOnRefreshListener {
            mainViewModel.getEquip()
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
                handler.postDelayed(Runnable {
                    val result = equipList.filter {
                        it.equipName?.contains(other = newText, ignoreCase = true) ?: false
                    }
                    equipListAdapter.submitList(result)
                },200)
                return true
            }

        })
    }

    private fun initObserver() {
        equipViewModel.getEquipList().observe(viewLifecycleOwner, {
            binding.equipProgressBar.visibility = View.GONE
            binding.equipSwipeRefreshLayout.isRefreshing = false
            equipListAdapter.submitList(it)
            equipList = it.toMutableList()
        })
    }

    private fun setUI() {
        binding.equipProgressBar.visibility = View.VISIBLE
        binding.equipSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary)
        binding.include.toolbar.title = "Оборудование"

        val menu = binding.include.toolbar.menu
        val menuItemSearch = menu?.findItem(R.id.action_search)

        searchView = menuItemSearch?.actionView as SearchView
        searchView?.queryHint = "Поиск оборудования"
    }

    private fun setRecyclerView() {
        equipListAdapter = EquipListAdapter()

        with(binding.equipRecyclerView) {
            adapter = equipListAdapter
            recycledViewPool.setMaxRecycledViews(0, EquipListAdapter.MAX_POOL_SIZE)
        }
        setClickListener()
    }

    private fun setClickListener() {
        equipListAdapter.onAddRFIDButtonClickListener = {

        }
        equipListAdapter.onCreateDocumentButtonListener = {

        }
    }
}