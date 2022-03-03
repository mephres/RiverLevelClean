package com.intas.metrolog.ui.equip

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEquipBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.equip.adapter.EquipListAdapter
import com.intas.metrolog.ui.equip.equip_menu.EquipMenuFragment
import com.intas.metrolog.ui.equip_document.EquipDocumentActivity
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.ui.scanner.NfcFragment
import com.intas.metrolog.util.Journal
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class EquipFragment : Fragment(R.layout.fragment_equip) {
    private lateinit var equipListAdapter: EquipListAdapter
    private var searchView: SearchView? = null
    private var equipList = mutableListOf<EquipItem>()

    private val binding by viewBinding(FragmentEquipBinding::bind)

    private val equipViewModel by lazy {
        ViewModelProvider(this)[EquipViewModel::class.java]
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

        binding.equipSwipeRefreshLayout.setOnRefreshListener {
            binding.equipSwipeRefreshLayout.isRefreshing = false
            binding.equipProgressIndicator.visibility = View.VISIBLE
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
                    setFilter(newText.trim())
                }, 300)
                return true
            }
        })
    }

    private fun setFilter(filter: String) {
        if (equipList.isNullOrEmpty()) {
            return
        }

        if (filter.isEmpty()) {
            if (equipListAdapter.currentList.size != equipList.size) {
                equipListAdapter.submitList(null)
            }
            equipListAdapter.submitList(equipList)

            return
        }

        val filterEquipList = mutableListOf<EquipItem>()
        for (equip in equipList) {
            if (equip.equipName?.contains(filter, true) == true ||
                equip.equipZavNum?.contains(filter, true) == true ||
                equip.equipTag?.contains(filter, true) == true ||
                equip.equipGRSI?.contains(filter, true) == true ||
                equip.equipVidIzm?.contains(filter, true) == true ||
                equip.equipZavodIzg?.contains(filter, true) == true ||
                equip.mestUstan?.contains(filter, true) == true
            ) {
                filterEquipList.add(equip)
            }
        }
        equipListAdapter.submitList(null)
        equipListAdapter.submitList(filterEquipList)
    }

    private fun initObserver() {
        lifecycleScope.launchWhenResumed {
            equipViewModel.equipList.observe(viewLifecycleOwner, {
                if (it.isNotEmpty()) {
                    binding.equipProgressIndicator.visibility = View.GONE
                    binding.equipSwipeRefreshLayout.isRefreshing = false
                    equipList = it.toMutableList()
                    equipListAdapter.submitList(it)
                }
            })

            mainViewModel.onErrorMessage.observe(viewLifecycleOwner, {
                showSnackBar(it)
                binding.equipSwipeRefreshLayout.isRefreshing = false
            })
        }
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

    private fun setUI() {
        binding.equipProgressIndicator.visibility = View.VISIBLE
        binding.equipSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary)
        binding.includeToolbar.toolbar.title = getString(R.string.bottom_menu_events_equip)

        val menu = binding.includeToolbar.toolbar.menu
        val menuItemSearch = menu?.findItem(R.id.action_search)

        searchView = menuItemSearch?.actionView as SearchView
        searchView?.queryHint = getString(R.string.equip_search_bar_hint)
    }

    private fun setRecyclerView() {
        equipListAdapter = EquipListAdapter()

        with(binding.equipRecyclerView) {
            adapter = equipListAdapter
            itemAnimator = null
            recycledViewPool.setMaxRecycledViews(0, EquipListAdapter.MAX_POOL_SIZE)
        }
        setClickListener()
    }

    private fun setClickListener() {
        equipListAdapter.onEquipItemClickListener = { equipItem ->
            val equipMenuFragment = EquipMenuFragment.newInstance(equipItem)
            equipMenuFragment.show(requireActivity().supportFragmentManager, EquipMenuFragment.EQUIP_MENU_TAG)

            Journal.insertJournal("EquipFragment->equipItemClick", "equip: $equipItem")

            equipMenuFragment.onAddRFIDClickListener = {
                val equip = it
                val nfcFragment = NfcFragment.newInstanceAddTag(equip)
                nfcFragment.show(requireActivity().supportFragmentManager, NfcFragment.NFC_FRAGMENT_TAG)

                Journal.insertJournal("EquipFragment->addRfid", "equip: $it")
            }
            equipMenuFragment.onCreateDocumentClickListener = {
                startActivity(EquipDocumentActivity.newIntent(requireContext(), it))

                Journal.insertJournal("NfcViewModel->createDocument", "equip: $it")
            }
        }
    }
}