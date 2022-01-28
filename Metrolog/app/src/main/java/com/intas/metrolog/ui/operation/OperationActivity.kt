package com.intas.metrolog.ui.operation

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityOperationBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.ui.operation.adapter.OperationListAdapter
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.ViewUtil

class OperationActivity : AppCompatActivity() {
    private var eventItem: EventItem? = null
    private var equipItem: EquipItem? = null
    private var equipFullInfoVisible = false
    private var operationMenuVisibleControl = false

    private lateinit var operationListAdapter: OperationListAdapter

    private val binding by lazy {
        ActivityOperationBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[OperationViewModel::class.java]
    }

    private val enterTransition = MaterialFadeThrough()
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim)
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim)
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim)
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim)
    }
    private val fromEnd: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_end_anim)
    }
    private val toEnd: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_end_anim)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        parseIntent()
        setToolbar()
        setObservers()
        setRecyclerView()
        setUi()

        binding.operationInfoImageView.setOnClickListener {
            showFullEquipInfo(it)
        }
        binding.operationControlFab.setOnClickListener {
            operationMenuVisibleControl = !operationMenuVisibleControl

            setControlButtonAnimation(operationMenuVisibleControl)
            configureControlButtonVisibility(operationMenuVisibleControl)
        }
        binding.shadowView.setOnClickListener {
            shadowViewClick()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setObservers() {
        val opId = eventItem?.opId ?: 0
        val equipId = eventItem?.equipId ?: 0
        val checkListSize = eventItem?.operationListSize ?: 0

        equipItem = viewModel.getEquip(equipId) ?: return

        if (checkListSize > 0) {
            viewModel.getCheckList(opId).observe(this, {
                operationListAdapter.submitList(it)
            })
        }
    }

    private fun shadowViewClick() {
        if (operationMenuVisibleControl) {
            binding.operationControlFab.shrink()

            binding.shadowView.visibility = View.GONE

            binding.startOperationFab.visibility = View.GONE
            binding.stopOperationFab.visibility = View.GONE
            binding.cancelOperationFab.visibility = View.GONE
            binding.completeOperationFab.visibility = View.GONE

            binding.startOperationFab.startAnimation(toBottom)
            binding.stopOperationFab.startAnimation(toBottom)
            binding.cancelOperationFab.startAnimation(toBottom)
            binding.completeOperationFab.startAnimation(toBottom)

            binding.startOperationTextView.startAnimation(toEnd)
            binding.stopOperationTextView.startAnimation(toEnd)
            binding.cancelOperationTextView.startAnimation(toEnd)
            binding.completeOperationTextView.startAnimation(toEnd)
        }
        operationMenuVisibleControl = !operationMenuVisibleControl
    }

    private fun setControlButtonAnimation(clicked: Boolean) {
        if (clicked) {
            binding.startOperationFab.startAnimation(fromBottom)
            binding.stopOperationFab.startAnimation(fromBottom)
            binding.cancelOperationFab.startAnimation(fromBottom)
            binding.completeOperationFab.startAnimation(fromBottom)

            binding.startOperationTextView.startAnimation(fromEnd)
            binding.stopOperationTextView.startAnimation(fromEnd)
            binding.cancelOperationTextView.startAnimation(fromEnd)
            binding.completeOperationTextView.startAnimation(fromEnd)
        } else {
            binding.startOperationFab.startAnimation(toBottom)
            binding.stopOperationFab.startAnimation(toBottom)
            binding.cancelOperationFab.startAnimation(toBottom)
            binding.completeOperationFab.startAnimation(toBottom)

            binding.startOperationTextView.startAnimation(toEnd)
            binding.stopOperationTextView.startAnimation(toEnd)
            binding.cancelOperationTextView.startAnimation(toEnd)
            binding.completeOperationTextView.startAnimation(toEnd)
        }
    }

    private fun configureControlButtonVisibility(clicked: Boolean) {
        if (clicked) {
            binding.operationControlFab.extend()
            binding.shadowView.visibility = View.VISIBLE

            when (eventItem?.status) {
                EventStatus.NEW -> {
                    binding.startOperationFab.visibility = View.VISIBLE
                    binding.startOperationTextView.visibility = View.VISIBLE
                    binding.cancelOperationFab.visibility = View.VISIBLE
                    binding.cancelOperationTextView.visibility = View.VISIBLE
                }
                EventStatus.IN_WORK -> {
                    binding.stopOperationFab.visibility = View.VISIBLE
                    binding.stopOperationTextView.visibility = View.VISIBLE
                    binding.completeOperationFab.visibility = View.VISIBLE
                    binding.completeOperationTextView.visibility = View.VISIBLE
                    binding.cancelOperationFab.visibility = View.VISIBLE
                    binding.cancelOperationTextView.visibility = View.VISIBLE
                }
                EventStatus.PAUSED -> {
                    binding.cancelOperationFab.visibility = View.VISIBLE
                    binding.cancelOperationTextView.visibility = View.VISIBLE
                }
            }
        } else {
            binding.operationControlFab.shrink()
            binding.shadowView.visibility = View.GONE

            binding.startOperationFab.visibility = View.GONE
            binding.stopOperationFab.visibility = View.GONE
            binding.cancelOperationFab.visibility = View.GONE
            binding.completeOperationFab.visibility = View.GONE

            binding.startOperationTextView.visibility = View.GONE
            binding.stopOperationTextView.visibility = View.GONE
            binding.cancelOperationTextView.visibility = View.GONE
            binding.completeOperationTextView.visibility = View.GONE
        }
    }

    private fun setUi() {
        binding.operationControlFab.shrink()

        when (eventItem?.status) {
            EventStatus.COMPLETED, EventStatus.CANCELED -> {
                binding.operationControlFab.visibility = View.GONE
            }
        }

        binding.equipNameTextView.text = eventItem?.equipName ?: getString(R.string.no_data)
        binding.operationNameTextView.text = eventItem?.name ?: getString(R.string.no_data)
        binding.equipRFIDTextView.text = equipItem?.equipRFID ?: getString(R.string.no_data)

        binding.equipZavNumTextView.text =
            if (!equipItem?.equipZavNum.isNullOrEmpty()) equipItem?.equipZavNum else getString(R.string.no_data)
        binding.equipTagTextView.text =
            if (!equipItem?.equipTag.isNullOrEmpty()) equipItem?.equipTag else getString(R.string.no_data)
        binding.equipLocationTextView.text =
            if (!equipItem?.mestUstan.isNullOrEmpty()) equipItem?.mestUstan else getString(R.string.no_data)
        binding.equipGRSITextView.text =
            if (!equipItem?.equipGRSI.isNullOrEmpty()) equipItem?.equipGRSI else getString(R.string.no_data)
        binding.equipManufacturerTextView.text =
            if (!equipItem?.equipZavodIzg.isNullOrEmpty()) equipItem?.equipZavodIzg else getString(R.string.no_data)
        binding.equipMeteringTypeTextView.text =
            if (!equipItem?.equipVidIzm.isNullOrEmpty()) equipItem?.equipVidIzm else getString(R.string.no_data)
        binding.equipCalibrationTextView.text =
            if (!equipItem?.lastCalibr.isNullOrEmpty()) equipItem?.lastCalibr else getString(R.string.no_data)
        binding.equipVerificationTextView.text =
            if (!equipItem?.lastVerif.isNullOrEmpty()) equipItem?.lastVerif else getString(R.string.no_data)

        binding.typeTextView.text = "Плановое"
        if (eventItem?.unscheduled != 0) {
            binding.typeTextView.text = "Внеплановое"
        }


        binding.planDateTextView.text =
            DateTimeUtil.getDateTimeFromMili(eventItem?.planDate ?: 0, "dd.MM.yyyy")

        fillTagActual()
        fillOperationStatus()
    }

    private fun fillTagActual() {
        when (equipItem?.equipTagActual) {
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

    private fun fillOperationStatus() {
        when (eventItem?.status) {
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
        if (!intent.hasExtra(EXTRA_EVENT_ITEM)) {
            finish()
            return
        }

        eventItem = intent.getParcelableExtra(EXTRA_EVENT_ITEM)
        if (eventItem == null) {
            finish()
            return
        }
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