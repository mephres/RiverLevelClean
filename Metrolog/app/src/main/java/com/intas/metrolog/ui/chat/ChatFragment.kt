package com.intas.metrolog.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private var searchView: SearchView? = null

    private val binding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }

    private val chatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
    }

    private fun setUI() {
        binding.chatProgressIndicator.visibility = View.VISIBLE
        binding.includeToolbar.toolbar.title = getString(R.string.bottom_menu_events_chat)

        val menu = binding.includeToolbar.toolbar.menu
        val menuItemSearch = menu?.findItem(R.id.action_search)

        searchView = menuItemSearch?.actionView as SearchView
        searchView?.queryHint = getString(R.string.search_title)
    }

}