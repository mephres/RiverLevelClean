package com.intas.metrolog.ui.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentRequestsBinding

class RequestsFragment : Fragment() {
    private var searchView: SearchView? = null

    private val binding by lazy {
        FragmentRequestsBinding.inflate(layoutInflater)
    }

    private lateinit var requestViewModel: RequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requestViewModel = ViewModelProvider(this)[RequestViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
    }

    private fun setUI() {
        binding.include.toolbar.title = "Заявки"

        val menu = binding.include.toolbar.menu
        val menuItemSearch = menu?.findItem(R.id.action_search)

        searchView = menuItemSearch?.actionView as SearchView
        searchView?.queryHint = "Поиск зявок"
    }
}