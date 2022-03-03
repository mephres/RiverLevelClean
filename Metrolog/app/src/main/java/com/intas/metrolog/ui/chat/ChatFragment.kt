package com.intas.metrolog.ui.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentChatBinding
import com.intas.metrolog.databinding.FragmentEquipBinding
import com.intas.metrolog.pojo.chat.ChatItem
import com.intas.metrolog.ui.chat.adapter.ChatListAdapter
import com.intas.metrolog.ui.chat.messages.MessageFragment
import com.intas.metrolog.ui.chat.select_user.SelectUserFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.launch

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private var searchView: SearchView? = null
    private lateinit var chatListAdapter: ChatListAdapter

    var chatItemList = mutableListOf<ChatItem>()

    private val binding by viewBinding(FragmentChatBinding::bind)

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
        setupRecyclerView()
        setupSearchViewListener()

        lifecycleScope.launch {

            chatViewModel.chatItemList.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    chatItemList = it.toMutableList()
                    chatListAdapter.submitList(it)
                }
                binding.equipProgressIndicator.visibility = View.GONE
            }

            binding.chatListSelectUserFab.setOnClickListener {
                val selectUserFragment = SelectUserFragment.newInstanceAddCompanion()
                selectUserFragment.show(
                    requireActivity().supportFragmentManager,
                    SelectUserFragment.SELECT_USER_FRAGMENT_TAG
                )
            }

            chatListAdapter.onCurrentListChangedListener = {
                binding.chatListRecyclerView.scrollToTop()
            }
        }
    }

    private fun setUI() {
        binding.includeToolbar.toolbar.title = getString(R.string.bottom_menu_events_chat)

        val menu = binding.includeToolbar.toolbar.menu
        val menuItemSearch = menu?.findItem(R.id.action_search)

        searchView = menuItemSearch?.actionView as SearchView
        searchView?.queryHint = getString(R.string.search_title)
    }

    private fun setupRecyclerView() {
        chatListAdapter = ChatListAdapter()
        with(binding.chatListRecyclerView) {
            adapter = chatListAdapter
            itemAnimator = null
            recycledViewPool.setMaxRecycledViews(0, ChatListAdapter.MAX_POOL_SIZE)
        }
        setupClickListener()
        setupScrollListener()
    }

    private fun setupClickListener() {
        chatListAdapter.onChatItemClickListener = {
            val args = Bundle().apply {
                putParcelable(MessageFragment.COMPANION_ITEM, it.companion)
            }
            findNavController().navigate(R.id.action_navigation_chat_to_messageFragment, args)
        }
    }

    private fun RecyclerView.scrollToTop() {

        postDelayed({
            val position = 0
            scrollToPosition(position)
        }, 200)
    }

    private fun setupScrollListener() {
        binding.chatListRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.chatListSelectUserFab.hide()
                } else {
                    binding.chatListSelectUserFab.show()
                }
            }
        })
    }

    private fun setupSearchViewListener() {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                setFilter(newText.trim())
                return true
            }
        })
    }

    private fun setFilter(text: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            chatListAdapter.submitList(chatItemList.filter {
                it.companion.fio?.contains(other = text, ignoreCase = true) ?: false ||
                        it.companion.position?.contains(text, true) ?: false
            })
        }, 200)
    }
}