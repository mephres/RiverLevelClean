package com.intas.metrolog.ui.operation

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityOperationBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.ui.operation.adapter.OperationListAdapter
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.ViewUtil
import java.util.*

class OperationActivity : AppCompatActivity() {
    private var eventId: Long = 0
    private var equipFullInfoVisible = false
    private var currentEventStatus: Int = 0

    private lateinit var operationListAdapter: OperationListAdapter

    private val binding by lazy {
        ActivityOperationBinding.inflate(layoutInflater)
    }

    private val viewModelFactory by lazy {
        OperationViewModelFactory(eventId, this.application)
    }

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[OperationViewModel::class.java]
    }

    private val enterTransition = MaterialFadeThrough()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        parseIntent()
        setToolbar()
        setRecyclerView()
        observeViewModel()
        setClickListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (viewModel.controlButtonClicked.value == true) {
            viewModel.changeControlButtonVisibleValue()
        } else {
            super.onBackPressed()
        }
    }

    private fun setClickListeners() {

        binding.operationInfoImageView.setOnClickListener {
            showFullEquipInfo(it)
        }

        binding.operationControlFab.setOnClickListener {
            viewModel.changeControlButtonVisibleValue()
        }

        binding.shadowView.setOnClickListener {
            viewModel.changeControlButtonVisibleValue()
        }

        binding.startOperationFab.setOnClickListener {
            viewModel.startTimer()
            viewModel.updateEventStatus(EventStatus.IN_WORK)
            viewModel.changeControlButtonVisibleValue()
        }

        binding.stopOperationFab.setOnClickListener {
            viewModel.stopTimer()
            viewModel.updateEventStatus(EventStatus.PAUSED)
            viewModel.changeControlButtonVisibleValue()
        }

        binding.cancelOperationFab.setOnClickListener {
            viewModel.updateEventStatus(EventStatus.CANCELED)
            viewModel.changeControlButtonVisibleValue()
            binding.operationControlFab.visibility = View.GONE
        }

        binding.completeOperationFab.setOnClickListener {
            viewModel.stopTimer()
            viewModel.updateEventStatus(EventStatus.COMPLETED)
            viewModel.changeControlButtonVisibleValue()
            binding.operationControlFab.visibility = View.GONE
        }
    }

    private fun observeViewModel() {

        viewModel.getCheckList().observe(this, { checkList ->
            if (checkList.isNullOrEmpty()) {
                showSnackBar(getString(R.string.operation_activity_empty_operations_list))
            } else {
                operationListAdapter.submitList(checkList)
            }
        })

        viewModel.eventItem.observe(this, { event ->
            setUi(event)
            fillEquipTagActual(event.equip)
            fillOperationStatus(event)

            currentEventStatus = event.status
        })

        viewModel.controlButtonClicked.observe(this, { click ->
            configureControlButtonVisibility(click)
        })

        viewModel.timerDuration.observe(this, {
            if (it > 0) {
                val strDate = DateTimeUtil.getTimerTimeFromMili(it)
                runOnUiThread {
                    binding.timerValueTextView.text = strDate
                }
                binding.timerValueTextView.visibility = View.VISIBLE
                binding.timerImageView.visibility = View.VISIBLE
            }
        })
    }

    private fun configureControlButtonVisibility(clicked: Boolean) {
        if (clicked) {
            when (currentEventStatus) {
                EventStatus.NEW -> {
                    binding.startOperationFab.show()
                    binding.startOperationTextView.visibility = View.VISIBLE

                    binding.cancelOperationFab.show()
                    binding.cancelOperationTextView.visibility = View.VISIBLE
                }
                EventStatus.IN_WORK -> {
                    binding.stopOperationFab.show()
                    binding.stopOperationTextView.visibility = View.VISIBLE

                    binding.completeOperationFab.show()
                    binding.completeOperationTextView.visibility = View.VISIBLE

                    binding.cancelOperationFab.show()
                    binding.cancelOperationTextView.visibility = View.VISIBLE
                }
                EventStatus.PAUSED -> {
                    binding.startOperationFab.show()
                    binding.startOperationTextView.text =
                        getString(R.string.operation_activity_pause_event_button_pause_state)
                    binding.startOperationTextView.visibility = View.VISIBLE

                    binding.cancelOperationFab.show()
                    binding.cancelOperationTextView.visibility = View.VISIBLE
                }
            }

            binding.operationControlFab.extend()
            binding.shadowView.visibility = View.VISIBLE
        } else {
            when (currentEventStatus) {
                EventStatus.COMPLETED, EventStatus.CANCELED -> {
                    binding.operationControlFab.visibility = View.GONE
                }
            }

            binding.startOperationFab.hide()
            binding.stopOperationFab.hide()
            binding.cancelOperationFab.hide()
            binding.completeOperationFab.hide()

            binding.startOperationTextView.visibility = View.GONE
            binding.stopOperationTextView.visibility = View.GONE
            binding.cancelOperationTextView.visibility = View.GONE
            binding.completeOperationTextView.visibility = View.GONE

            binding.operationControlFab.shrink()
            binding.shadowView.visibility = View.GONE
        }
    }

    private fun setUi(event: EventItem) {
        val equip = event.equip

        binding.equipNameTextView.text =
            if (!equip?.equipName.isNullOrEmpty()) equip?.equipName else getString(R.string.no_data)
        binding.equipRFIDTextView.text =
            if (!equip?.equipRFID.isNullOrEmpty()) equip?.equipRFID else getString(R.string.no_data)
        binding.equipZavNumTextView.text =
            if (!equip?.equipZavNum.isNullOrEmpty()) equip?.equipZavNum else getString(R.string.no_data)
        binding.equipTagTextView.text =
            if (!equip?.equipTag.isNullOrEmpty()) equip?.equipTag else getString(R.string.no_data)
        binding.equipLocationTextView.text =
            if (!equip?.mestUstan.isNullOrEmpty()) equip?.mestUstan else getString(R.string.no_data)
        binding.equipGRSITextView.text =
            if (!equip?.equipGRSI.isNullOrEmpty()) equip?.equipGRSI else getString(R.string.no_data)
        binding.equipManufacturerTextView.text =
            if (!equip?.equipZavodIzg.isNullOrEmpty()) equip?.equipZavodIzg else getString(R.string.no_data)
        binding.equipMeteringTypeTextView.text =
            if (!equip?.equipVidIzm.isNullOrEmpty()) equip?.equipVidIzm else getString(R.string.no_data)
        binding.equipCalibrationTextView.text =
            if (!equip?.lastCalibr.isNullOrEmpty()) equip?.lastCalibr else getString(R.string.no_data)
        binding.equipVerificationTextView.text =
            if (!equip?.lastVerif.isNullOrEmpty()) equip?.lastVerif else getString(R.string.no_data)

        binding.operationNameTextView.text = event.name ?: getString(R.string.no_data)
        binding.planDateTextView.text =
            DateTimeUtil.getDateTimeFromMili(event.planDate ?: 0, "dd.MM.yyyy")

        binding.typeTextView.text = "Плановое"
        if (event.unscheduled != 0) {
            binding.typeTextView.text = "Внеплановое"
        }
    }

    private fun fillEquipTagActual(equip: EquipItem?) {
        when (equip?.equipTagActual) {
            0 -> {
                binding.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_baseline_cancel_black_24dp
                    )
                )
                binding.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.md_red_600
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
            1 -> {
                binding.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_baseline_check_circle_black_24dp
                    )
                )
                binding.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.colorPrimary
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
            else -> {
                binding.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_warning_red_24dp
                    )
                )
                binding.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.md_deep_orange_A200
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
        }
    }

    private fun fillOperationStatus(event: EventItem) {
        when (event.status) {
            EventStatus.NEW -> {
                binding.statusTextView.text = "Можно выполнить"
            }
            EventStatus.IN_WORK -> {
                binding.statusTextView.text = "Выполняется"
            }
            EventStatus.PAUSED -> {
                binding.statusTextView.text = "Остановлено"
            }
            EventStatus.COMPLETED -> {
                binding.statusTextView.text = "Завершено"
            }
            EventStatus.CANCELED -> {
                binding.statusTextView.text = "Отменено"
            }
        }
    }

    private fun showFullEquipInfo(view: View) {
        ViewUtil.runAnimationButton(applicationContext, view)

        equipFullInfoVisible = !equipFullInfoVisible

        if (equipFullInfoVisible) {
            TransitionManager.beginDelayedTransition(
                binding.root,
                enterTransition
            )
            binding.operationInfoImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_chevron_up
                )
            )

            binding.equipRFIDLabelTextView.visibility = View.VISIBLE
            binding.equipRFIDTextView.visibility = View.VISIBLE
            binding.equipGRSILabelTextView.visibility = View.VISIBLE
            binding.equipGRSITextView.visibility = View.VISIBLE
            binding.equipManufacturerLabelTextView.visibility = View.VISIBLE
            binding.equipManufacturerTextView.visibility = View.VISIBLE
            binding.equipMeteringTypeLabelTextView.visibility = View.VISIBLE
            binding.equipMeteringTypeTextView.visibility = View.VISIBLE
            binding.equipCalibrationLabelTextView.visibility = View.VISIBLE
            binding.equipCalibrationTextView.visibility = View.VISIBLE
            binding.equipVerificationLabelTextView.visibility = View.VISIBLE
            binding.equipVerificationTextView.visibility = View.VISIBLE
        } else {
            TransitionManager.endTransitions(binding.root)
            binding.operationInfoImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_chevron_down
                )
            )

            binding.equipRFIDLabelTextView.visibility = View.GONE
            binding.equipRFIDTextView.visibility = View.GONE
            binding.equipGRSILabelTextView.visibility = View.GONE
            binding.equipGRSITextView.visibility = View.GONE
            binding.equipManufacturerLabelTextView.visibility = View.GONE
            binding.equipManufacturerTextView.visibility = View.GONE
            binding.equipMeteringTypeLabelTextView.visibility = View.GONE
            binding.equipMeteringTypeTextView.visibility = View.GONE
            binding.equipCalibrationLabelTextView.visibility = View.GONE
            binding.equipCalibrationTextView.visibility = View.GONE
            binding.equipVerificationLabelTextView.visibility = View.GONE
            binding.equipVerificationTextView.visibility = View.GONE
        }
    }

    private fun setRecyclerView() {
        operationListAdapter = OperationListAdapter()

        with(binding.operationRecyclerView) {
            adapter = operationListAdapter
            recycledViewPool.setMaxRecycledViews(0, OperationListAdapter.MAX_POOL_SIZE)
        }
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

    private fun parseIntent() {
        if (!intent.hasExtra(EVENT_ID)) {
            finish()
            return
        }

        eventId = intent.getLongExtra(EVENT_ID, 0)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { }.show()
    }

    companion object {

        private const val EVENT_ID = "event_item"

        fun newIntent(context: Context, eventId: Long): Intent {
            val intent = Intent(context, OperationActivity::class.java)
            intent.putExtra(EVENT_ID, eventId)
            return intent
        }
    }
}