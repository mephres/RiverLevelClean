package com.intas.metrolog.ui.equip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.databinding.FragmentEquipBinding

class EquipFragment : Fragment() {

    private val binding by lazy {
        FragmentEquipBinding.inflate(layoutInflater)
    }

    private lateinit var equipViewModel: EquipViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        equipViewModel = ViewModelProvider(this)[EquipViewModel::class.java]
        return binding.root
    }

}