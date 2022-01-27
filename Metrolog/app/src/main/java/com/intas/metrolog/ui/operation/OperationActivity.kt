package com.intas.metrolog.ui.operation

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityOperationBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.ui.equip_document.EquipDocumentActivity

class OperationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityOperationBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[OperationViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setToolbar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setToolbar() {
        setSupportActionBar(binding.operationToolbar)
        binding.operationToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.md_black))
        binding.operationToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white))
        supportActionBar?.let {
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24dp)
            it.setDisplayHomeAsUpEnabled(true)
        }
        this.title = "Мероприятие"
    }

    companion object {

        private const val EXTRA_EVENT_ITEM = "event_item"

        fun newIntent(context: Context, eventItem: EventItem): Intent {
            val intent = Intent(context, OperationActivity::class.java)
            intent.putExtra(EXTRA_EVENT_ITEM, eventItem)
            return intent
        }
    }
}