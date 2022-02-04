package com.intas.metrolog.ui.chat.select_user

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentSelectUserBinding
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.chat.MessageItem
import com.intas.metrolog.ui.chat.messages.MessageFragment
import com.intas.metrolog.ui.chat.select_user.adapter.UserListAdapter
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util

class SelectUserFragment : BottomSheetDialogFragment() {
    private var searchView: SearchView? = null
    private lateinit var userListAdapter: UserListAdapter

    private var screenMode: String = MODE_UNKNOWN
    private var chatUserList = mutableListOf<UserItem>()
    private var forwardedMessage: MessageItem? = null

    private var _binding: FragmentSelectUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SelectUserViewModel by viewModels()

    private val modes = arrayOf(
        MODE_FORWARD_MESSAGE, MODE_ADD_COMPANION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
        setupRecyclerView()
        setupSearchViewListener()
        observeUsers()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)
        return dialog
    }

    private fun observeUsers() {
        viewModel.chatUserList.observe(viewLifecycleOwner) {

            chatUserList = it.filter {
                it.id != Util.authUser?.userId
            }.toMutableList()

            val usersCount = when (chatUserList.count()) {
                1, in 21..101 step 10, in 121..201 step 10 -> "${chatUserList.count()} контакт"
                in 2..4, in 22..102 step 10, in 122..202 step 10, in 23..103 step 10,
                in 123..203 step 10, in 24..104 step 10,
                in 124..204 step 10 -> "${chatUserList.count()} контакта"
                else -> "${chatUserList.count()} контактов"
            }

            binding.selectUserToolbar.subtitle = usersCount
            userListAdapter.submitList(chatUserList)
        }
    }

    private fun setUI() {
        binding.selectUserToolbar.title = "Выбрать"
        binding.selectUserToolbar.setTitleTextAppearance(
            requireContext(),
            R.style.Toolbar_TitleText
        )
        binding.selectUserToolbar.setSubtitleTextAppearance(
            requireContext(),
            R.style.Toolbar_SubTitleText
        )

        val menu = binding.selectUserToolbar.menu
        val menuItemSearch = menu?.findItem(R.id.action_search)

        searchView = menuItemSearch?.actionView as SearchView
        searchView?.queryHint = getString(R.string.search_title)
    }

    private fun setupRecyclerView() {
        userListAdapter = UserListAdapter()
        with(binding.chatUserRecyclerView) {
            adapter = userListAdapter
            recycledViewPool.setMaxRecycledViews(0, UserListAdapter.MAX_POOL_SIZE)
        }
        setupClickListener()
    }

    private fun setupClickListener() {
        userListAdapter.onUserItemClickListener = { companion ->
            launchMode(companion)
        }
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
            userListAdapter.submitList(chatUserList.filter {
                it.fio?.contains(text, true) ?: false ||
                        it.position?.contains(text, true) ?: false
            })
        }, 200)
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(SELECT_USER_FRAGMENT_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun launchMode(companion: UserItem) {
        when (screenMode) {
            MODE_ADD_COMPANION -> {
                val args = Bundle().apply {
                    putParcelable(MessageFragment.COMPANION_ITEM, companion)
                }
                findNavController().navigate(R.id.messageFragment, args)
                closeFragment()
            }

            MODE_FORWARD_MESSAGE -> {
                forwardedMessage?.let {
                    val message = MessageItem(
                        message = it.message,
                        senderUserId = Util.authUser?.userId,
                        companionUserId = companion.id,
                        isSent = 0,
                        isViewed = 1,
                        dateTime = DateTimeUtil.getUnixDateTimeNow(),
                        isForwarded = 1
                    )
                    viewModel.insertMessage(message)
                }
                findNavController().popBackStack()
                closeFragment()
            }
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(SCREEN_MODE)) {
            return
        }
        val mode = args.getString(SCREEN_MODE)
        if (!modes.contains(mode)) {
            return
        }
        mode?.let { screenMode = it }
        if (screenMode == MODE_FORWARD_MESSAGE) {
            if (!args.containsKey(FORWARDED_MESSAGE)) {
                return
            }
            forwardedMessage = args.getParcelable(FORWARDED_MESSAGE)

            if (forwardedMessage == null) {
                return
            }
        }
    }

    companion object {
        const val SELECT_USER_FRAGMENT_TAG = "select_user_fragment_tag"
        private const val SCREEN_MODE = "mode"
        private const val MODE_UNKNOWN = ""
        private const val MODE_ADD_COMPANION = "mode_add_companion"
        private const val MODE_FORWARD_MESSAGE = "mode_forward_message"
        private const val FORWARDED_MESSAGE = "forwarded_message"

        fun newInstanceAddCompanion() = SelectUserFragment().apply {
            arguments = Bundle().apply {
                putString(SCREEN_MODE, MODE_ADD_COMPANION)
            }
        }

        fun newInstanceForwardMessage(message: MessageItem) = SelectUserFragment().apply {
            arguments = Bundle().apply {
                putString(SCREEN_MODE, MODE_FORWARD_MESSAGE)
                putParcelable(FORWARDED_MESSAGE, message)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}