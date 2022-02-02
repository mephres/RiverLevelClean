package com.intas.metrolog.ui.chat.messages

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentMessageBinding
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.chat.MessageItem
import com.intas.metrolog.ui.chat.messages.adapter.MessageListAdapter
import com.intas.metrolog.ui.chat.select_user.SelectUserFragment
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util

class MessageFragment : Fragment() {
    private lateinit var messageListAdapter: MessageListAdapter
    private lateinit var companion: UserItem
    private var onMenuItemClickListener: ((MenuItem, MessageItem) -> Unit)? = null

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val viewModelFactory by lazy {
        MessagesViewModelFactory(companion, requireActivity().application)
    }

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[MessageViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        messageSendButtonEnable(false)
        setClickListeners()

        viewModel.getMessageList().observe(viewLifecycleOwner) {
            messageListAdapter.submitList(it)
            binding.messageRecyclerView.scrollToBottom()
            viewModel.setChatMessageViewed(companion.id)
        }

        binding.chatMessageTextView.doOnTextChanged { _, _, _, count ->
            messageSendButtonEnable(count > 0)
        }

        binding.userName.text = companion.fio
        binding.userPosition.text = companion.position
        Glide.with(requireContext()).load(R.drawable.ic_worker).circleCrop()
            .into(binding.userPhoto)
    }

    private fun setClickListeners() {
        binding.backToChatListImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.sendMessageFab.setOnClickListener {
            val messageText =
                binding.chatMessageTextView.text.toString().trim()

            if (messageText.isEmpty()) {
                return@setOnClickListener
            }
            sendMessage(messageText)
            binding.chatMessageTextView.text.clear()
        }

        messageListAdapter.onMessageItemLongClickListener = { view, messageItem ->
            showMenu(view, R.menu.message_item_menu, messageItem)
        }

        onMenuItemClickListener = { menuItem, messageItem ->

            when (menuItem.itemId) {

                R.id.message_item_action_forward_message -> {
                    forwardMessage(messageItem)
                }

                R.id.message_item_action_delete_message -> {
                    if (Util.authUser?.userId == messageItem.senderUserId) {
                        if (messageItem.isSent == 0) {
                            viewModel.deleteMessageBy(messageItem.id ?: 0)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, menuRes: Int, messageItem: MessageItem) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener {
            onMenuItemClickListener?.invoke(it, messageItem)
            true
        }

        if (popup.menu is MenuBuilder) {

            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)

            val iconMargin = 8

            for (item in menuBuilder.visibleItems) {

                if (Util.authUser?.userId != messageItem.senderUserId && item.itemId == R.id.message_item_action_delete_message) {
                    item.isVisible = false
                    continue
                }
                if (Util.authUser?.userId == messageItem.senderUserId && messageItem.isSent == 1 && item.itemId == R.id.message_item_action_delete_message) {
                    item.isVisible = false
                    continue
                }

                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        iconMargin.toFloat(),
                        resources.displayMetrics
                    )
                        .toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                    } else {
                        item.icon =
                            object :
                                InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }
        }
        popup.show()
    }

    private fun sendMessage(text: String) {
        val senderId = Util.authUser?.userId
        val companionId = companion.id
        val message = MessageItem(
            message = text,
            senderUserId = senderId,
            companionUserId = companionId,
            isSent = 0,
            isViewed = 1,
            dateTime = DateTimeUtil.getUnixDateTimeNow()
        )
        viewModel.insertMessage(message)
        binding.messageRecyclerView.scrollToBottom()
    }

    private fun forwardMessage(messageItem: MessageItem) {
        val selectUserFragment = SelectUserFragment.newInstanceForwardMessage(messageItem)
        selectUserFragment.show(
            requireActivity().supportFragmentManager,
            SelectUserFragment.SELECT_USER_FRAGMENT_TAG
        )
    }

    private fun messageSendButtonEnable(enabled: Boolean) {

        binding.sendMessageFab.isEnabled = enabled
        if (enabled) {
            binding.sendMessageFab.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent
                )
            )
        } else {
            binding.sendMessageFab.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_grey_400
                )
            )
        }
    }

    private fun setupRecyclerView() {
        messageListAdapter = MessageListAdapter()

        with(binding.messageRecyclerView) {
            itemAnimator = null
            adapter = messageListAdapter
            recycledViewPool.setMaxRecycledViews(0, MessageListAdapter.MAX_POOL_SIZE)
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(COMPANION_ITEM)) {
            return
        }

        args.getParcelable<UserItem>(COMPANION_ITEM)?.let {
            companion = it
        }
    }

    private fun RecyclerView.scrollToBottom() {
        postDelayed({
            var position = 0
            adapter?.let {
                position = it.itemCount - 1
            }
            scrollToPosition(position)
        }, 100)
    }

    companion object {
        const val COMPANION_ITEM = "companion_item"
    }
}