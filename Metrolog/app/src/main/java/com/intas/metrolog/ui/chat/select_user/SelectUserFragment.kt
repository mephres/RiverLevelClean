package com.intas.metrolog.ui.chat.select_user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentSelectUserBinding
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.ui.chat.select_user.adapter.UserListAdapter
import com.intas.metrolog.util.Util

class SelectUserFragment : Fragment() {
    private var searchView: SearchView? = null
    private lateinit var userListAdapter: UserListAdapter

    private var chatUserList = mutableListOf<UserItem>()

    private val binding by lazy {
        FragmentSelectUserBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[SelectUserViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
        setupRecyclerView()

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

            binding.includeToolbar.toolbar.subtitle = usersCount
            userListAdapter.submitList(chatUserList)
        }

        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setUI() {
        binding.includeToolbar.toolbar.title = "Выбрать"
        binding.includeToolbar.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_arrow_back_24dp)
        binding.includeToolbar.toolbar.setTitleTextAppearance(requireContext(), R.style.Toolbar_TitleText)
        binding.includeToolbar.toolbar.setSubtitleTextAppearance(requireContext(), R.style.Toolbar_SubTitleText)

        val menu = binding.includeToolbar.toolbar.menu
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

        }
    }
}