package com.intas.metrolog.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private val binding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }

    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        return binding.root
    }

}