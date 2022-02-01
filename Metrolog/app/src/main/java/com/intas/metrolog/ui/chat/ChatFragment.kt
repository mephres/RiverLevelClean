package com.intas.metrolog.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentChatBinding
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.chat.ChatItem
import com.intas.metrolog.ui.chat.adapter.ChatListAdapter
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util

class ChatFragment : Fragment() {
    private var searchView: SearchView? = null
    private lateinit var chatListAdapter: ChatListAdapter

    private val binding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }

    private val chatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }

    private val _chatItemList = MutableLiveData<List<ChatItem>>()
    private val chatItemList: LiveData<List<ChatItem>>
        get() = _chatItemList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        setupRecyclerView()

        chatViewModel.messageList.observe(viewLifecycleOwner) { messages ->
            binding.chatProgressIndicator.isVisible = messages.isEmpty()

            val chatItemList = mutableListOf<ChatItem>()

            messages.forEach { message ->

                message.senderUserId?.let { messageSenderId ->
                    var messageNotViewedCount = 0
                    val companion: UserItem?
                    val currentUserId = Util.authUser?.userId

                    if (messageSenderId != currentUserId) {
                        companion = chatViewModel.getCompanionById(messageSenderId)
                        messageNotViewedCount = chatViewModel.getNotViewedMessagesCount(
                            messageSenderId,
                            currentUserId ?: 0
                        )
                    } else {
                        val companionId = message.companionUserId ?: 0
                        companion = chatViewModel.getCompanionById(companionId)
                    }

                    val userName = companion?.fio ?: ""
                    val messageText = message.message ?: ""
                    val companionId = companion?.id ?: 0
                    val companionPosition = companion?.position  ?: ""
                    val messageId = message.id ?: 0
                    val messageDateTime = DateTimeUtil.getShortDataFromMili(message.dateTime ?: 0)

                    val chatItem = ChatItem(
                        id = messageId,
                        userName = userName,
                        lastMessage = messageText,
                        notViewedMessageCount = messageNotViewedCount,
                        companionId = companionId,
                        userPosition = companionPosition,
                        lastMessageDate = messageDateTime
                    )
                    chatItemList.add(chatItem)
                }
                chatItemList.removeAll { chatItem -> chatItemList.any { chatItem.companionId == it.companionId && it.id > chatItem.id} }
                chatItemList.sortByDescending {
                    it.lastMessageDate
                }
            }
            this._chatItemList.value = chatItemList
        }

        this.chatItemList.observe(viewLifecycleOwner) {
            chatListAdapter.submitList(it)
        }

        binding.chatListSelectUserFab.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_chat_to_selectUserFragment)
        }
    }

    private fun setUI() {
        binding.chatProgressIndicator.visibility = View.VISIBLE
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
                recycledViewPool.setMaxRecycledViews(0, ChatListAdapter.MAX_POOL_SIZE)
            }
        setupClickListener()
        setupScrollListener()
    }

    private fun setupClickListener() {
        chatListAdapter.onChatItemClickListener = {

        }
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
}