package me.kdv.riverlevel.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import me.kdv.riverlevel.R
import me.kdv.riverlevel.databinding.ActivityMainBinding
import me.kdv.riverlevel.presentation.adapters.RiverInfoAdapter
import me.kdv.riverlevel.presentation.adapters.RiverInfoAdapter.Companion.MAX_POOL_SIZE
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: RiverViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val component by lazy {
        (application as RiverApp).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, viewModelFactory)[RiverViewModel::class.java]

        val adapter = RiverInfoAdapter(this)

        with(binding.riverListRecyclerView) {
            this.adapter = adapter
            recycledViewPool.setMaxRecycledViews(0, MAX_POOL_SIZE)
        }

        adapter.onRiverItemClickListener = {

        }

        viewModel.riverInfoList.observe(this) {
            adapter.submitList(it)
        }
    }
}