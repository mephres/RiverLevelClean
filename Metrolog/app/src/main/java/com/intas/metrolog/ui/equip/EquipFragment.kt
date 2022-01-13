package com.intas.metrolog.ui.equip

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
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
                handler.postDelayed({
                    equipListAdapter.submitList(equipList.filter {
                        it.equipName?.contains(other = newText.trim(), ignoreCase = true) == true ||
                                it.equipZavNum?.contains(other = newText.trim(), ignoreCase = true) == true ||
                                it.equipTag?.contains(other = newText.trim(), ignoreCase = true) == true ||
                                it.equipGRSI?.contains(other = newText.trim(), ignoreCase = true) == true ||
                                it.equipVidIzm?.contains(other = newText.trim(), ignoreCase = true) == true ||
                                it.equipZavodIzg?.contains(other = newText.trim(), ignoreCase = true) == true ||
                                it.mestUstan?.contains(other = newText.trim(), ignoreCase = true) == true
                    })
                },200)
                return true
            }

        })
    }

    private fun initObserver() {
        equipViewModel.getEquipList().observe(viewLifecycleOwner, {
            binding.equipProgressBar.visibility = View.GONE
            binding.equipSwipeRefreshLayout.isRefreshing = false
            equipList = it.toMutableList()
            equipListAdapter.submitList(equipList)
        })

        mainViewModel.onErrorMessage.observe(viewLifecycleOwner, {
            showSnackBar(it)
            binding.equipSwipeRefreshLayout.isRefreshing = false
        })
    }

    private fun showSnackBar(message: String) {
        val snackbar =
            Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("OK") {
            snackbar.dismiss()
        }
        val messageView: TextView = snackbar.view.findViewById(R.id.snackbar_text)
        messageView.maxLines = 20
        snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
        snackbar.show()
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