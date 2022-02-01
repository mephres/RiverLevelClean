package com.intas.metrolog.ui.chat.messages

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentMessageBinding
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.ui.chat.messages.adapter.MessageListAdapter

class MessageFragment : Fragment() {
    private lateinit var messageListAdapter: MessageListAdapter
    private lateinit var companion: UserItem

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
        }

        binding.chatMessageTextView.doOnTextChanged { _, _, _, count ->
            messageSendButtonEnable(count > 0)
        }

        binding.userName.text = companion.fio
        binding.userPosition.text = companion.position
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
    }

    private fun sendMessage(text: String) {

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

    companion object {
        const val COMPANION_ITEM = "companion_item"

        fun newInstance(companionId: Int) =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putInt(COMPANION_ITEM, companionId)
                }
            }
    }
}