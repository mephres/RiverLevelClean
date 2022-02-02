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
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentSelectUserBinding
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.ui.chat.messages.MessageFragment
import com.intas.metrolog.ui.chat.select_user.adapter.UserListAdapter
import com.intas.metrolog.util.Util

class SelectUserFragment : BottomSheetDialogFragment() {
    private var searchView: SearchView? = null
    private lateinit var userListAdapter: UserListAdapter

    private var chatUserList = mutableListOf<UserItem>()

    private var _binding: FragmentSelectUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SelectUserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding.selectUserToolbar.setNavigationOnClickListener {
            closeFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)
        return dialog
    }

    private fun observeUsers() {
        viewModel.chatUserList.observe(
            viewLifecycleOwner
        ) {

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
        binding.selectUserToolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_arrow_back_24dp)
        binding.selectUserToolbar.setTitleTextAppearance(requireContext(), R.style.Toolbar_TitleText)
        binding.selectUserToolbar.setSubtitleTextAppearance(requireContext(), R.style.Toolbar_SubTitleText)

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
        userListAdapter.onUserItemClickListener = {
            val args = Bundle().apply {
                putParcelable(MessageFragment.COMPANION_ITEM, it)
            }
            findNavController().navigate(R.id.messageFragment, args)
            closeFragment()
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

    companion object {
        const val SELECT_USER_FRAGMENT_TAG = "select_user_fragment_tag"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}