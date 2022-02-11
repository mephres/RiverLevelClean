package com.intas.metrolog.ui.operation

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.transition.TransitionManager
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityOperationBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_priority.EventPriority
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.CANCELED
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.COMPLETED
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.IN_WORK
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.NEW
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.PAUSED
import com.intas.metrolog.ui.events.event_comment.EventCommentFragment
import com.intas.metrolog.ui.events.event_comment.EventCommentFragment.Companion.EVENT_COMMENT_FRAGMENT_TAG
import com.intas.metrolog.ui.events.select_event.SelectEventFragment
import com.intas.metrolog.ui.operation.adapter.OperationListAdapter
import com.intas.metrolog.ui.operation.adapter.callback.EventOperationItemTouchHelperCallback
import com.intas.metrolog.ui.operation.equip_info.EquipInfoFragment
import com.intas.metrolog.ui.operation.operation_control.OperationControlInputValueFragment
import com.intas.metrolog.ui.operation.operation_control.OperationControlInputValueFragment.Companion.OPERATION_CONTROL_FRAGMENT_TAG
import com.intas.metrolog.ui.scanner.NfcFragment
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util
import com.intas.metrolog.util.ViewUtil

class OperationActivity : AppCompatActivity() {
    private var eventId: Long = 0

    /**
     * Признак, нужно ли дополнительное чтение метки для начала выполнения мероприятия
     * 0 - не нужно (если мероприятие открыто с помощью сканера)
     * 1 - нужно (если мероприятие открыто из общего списка мероприятий)
     */
    private var needVerify: Boolean = true
    private var equipFullInfoVisible = false
    private var currentEventStatus: Int = 0
    private var currentEvent: EventItem? = null
    private var currentEquip: EquipItem? = null
    private var equipInfoIsShowing = false

    private lateinit var operationListAdapter: OperationListAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

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
        setSwipeListener()
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
        Journal.insertJournal("OperationActivity->onBackPressed", "")
        if (viewModel.controlButtonClicked.value == true) {
            viewModel.changeControlButtonVisibleValue()
        } else {
            super.onBackPressed()
        }
    }

    private fun setClickListeners() {

        binding.operationInfoImageView.setOnClickListener {
            Journal.insertJournal("OperationActivity->setClickListeners", "operationInfoImageView.setOnClickListener")
            showFullEquipInfo(it)
        }

        binding.eventControlFab.setOnClickListener {
            Journal.insertJournal("OperationActivity->setClickListeners", "eventControlFab.setOnClickListener")
            viewModel.changeControlButtonVisibleValue()
        }

        binding.shadowView.setOnClickListener {
            Journal.insertJournal("OperationActivity->setClickListeners", "shadowView.setOnClickListener")
            viewModel.changeControlButtonVisibleValue()
        }

        binding.startEventFab.setOnClickListener {
            startEvent()
        }

        binding.stopEventFab.setOnClickListener {
            Journal.insertJournal("OperationActivity->setClickListeners", "stopEventFab.setOnClickListener")
            pauseEvent()
            viewModel.changeControlButtonVisibleValue()
        }

        binding.cancelEventFab.setOnClickListener {
            Journal.insertJournal("OperationActivity->setClickListeners", "cancelEventFab.setOnClickListener")
            showEventComment(CANCELED)
            viewModel.changeControlButtonVisibleValue()
            binding.eventControlFab.visibility = View.GONE
        }

        binding.completeEventFab.setOnClickListener {
            Journal.insertJournal("OperationActivity->setClickListeners", "completeEventFab.setOnClickListener")
            operationListAdapter.currentList.let {
                it.forEach { eoi ->
                    if (eoi.completed == 0) {
                        showToast(getString(R.string.operation_activity_complete_event_error))
                        viewModel.changeControlButtonVisibleValue()
                        return@setOnClickListener
                    }
                }
            }
            showEventComment(COMPLETED)
            viewModel.changeControlButtonVisibleValue()
        }
    }

    private fun initTouchHelper() {

        if (currentEvent?.status != IN_WORK) return

        val callback = EventOperationItemTouchHelperCallback(operationListAdapter, true)

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.operationRecyclerView)
    }

    /**
     * Слушатель свапа операции мероприятия
     */
    private fun setSwipeListener() {

        operationListAdapter.onSwiped = {eventOperation->
            Journal.insertJournal("OperationActivity->setSwipeListener->eventOperation", eventOperation)

            // если у данной операции есть операционный контроль
            if (eventOperation.hasOperationControl) {
                // запуск фрагмента с операционным контролем
                val operationControlFragment = OperationControlInputValueFragment.newInstance(eventOperation.subId)
                operationControlFragment.show(supportFragmentManager, OPERATION_CONTROL_FRAGMENT_TAG)

                // слушатель на нажатие кнопки Сохранить
                operationControlFragment.onSaveValueListener = {
                    if (it) {
                        Journal.insertJournal("OperationActivity->setSwipeListener->onSaveValueListener", eventOperation)
                        // делаем операцию выполненной
                        viewModel.setOperationComplete(eventOperation)
                        // если список в адаптере не пустой и количество элементов списка равно одному,
                        // то считаем, что был свайп последней операции мероприятия и выводим фрагмент комментария
                        if (!operationListAdapter.currentList.isNullOrEmpty() && operationListAdapter.currentList.size == 1) {
                            // считаем, что мероприятие выполнено, показ фрагмента комментария
                            showEventComment(COMPLETED)
                        }
                    }
                }
            } else {
                // выполнение операции
                viewModel.setOperationComplete(eventOperation)
                // если список в адаптере не пустой и количество элементов списка равно одному,
                // то считаем, что был свайп последней операции мероприятия и выводим фрагмент комментария
                if (!operationListAdapter.currentList.isNullOrEmpty() && operationListAdapter.currentList.size == 1) {
                    // считаем, что мероприятие выполнено, показ фрагмента комментария
                    showEventComment(COMPLETED)
                }
            }
        }
    }

    private fun observeViewModel() {

        viewModel.eventItem.observe(this, { event ->

            Journal.insertJournal("OperationActivity->showEventComment->event", event)
            viewModel.getEquipById(event.equipId ?: 0).observe(this, { equip ->
                event.equip = equip
                setUi(event)
                fillEquipTagActual(equip)
                fillOperationStatus(event)
                currentEquip = equip
                currentEvent?.equip = equip
                showEquipInfo(equip)
                Journal.insertJournal("OperationActivity->showEventComment->equip", equip)
            })
            currentEvent = event
            currentEventStatus = event.status
            setTimer(currentEventStatus)
            loadOperationList()
            initTouchHelper()
            controlButtons()
        })

        // отображение значений таймера
        viewModel.timerDuration.observe(this, {
            if (it >= 0) {
                val strDate = DateTimeUtil.getTimerTimeFromMili(it)
                runOnUiThread {
                    binding.timerValueTextView.text = strDate
                }
                binding.timerValueTextView.visibility = View.VISIBLE
                binding.timerImageView.visibility = View.VISIBLE
            }
        })
    }

    /**
     * Отображение фрагмента ввода комментария и фотофиксации
     * @param status - статус мероприятия, в котором оно должно оказаться после ввода комментария
     */
    private fun showEventComment(status: Int) {

        Journal.insertJournal("OperationActivity->showEventComment", status)

        currentEvent?.let {
            Journal.insertJournal("OperationActivity->showEventComment->currentEvent", it)

            val eventCommentFragment = if (currentEvent?.needPhotoFix == true) {
                // если фотофиксация нужна, запускаем фрагмент с возможностью добавить изображение фотофиксации
                EventCommentFragment.newInstanceWithImage(it.opId, status)
            } else {
                EventCommentFragment.newInstanceWithoutImage(it.opId, status)
            }
            eventCommentFragment.show(supportFragmentManager, EVENT_COMMENT_FRAGMENT_TAG)
            eventCommentFragment.onSaveCommentListener = { comment, eventStatus ->
                // получение статуса мероприятия и текста комментария
                when (eventStatus) {
                    COMPLETED, CANCELED -> finishEvent(eventStatus, comment)
                }
            }
        }
    }

    private fun controlButtons() {
        viewModel.controlButtonClicked.observe(this, { click ->
            configureControlButtonVisibility(click)
        })
    }

    /**
     * Загрузка списка операций мероприятия
     */
    private fun loadOperationList() {

        viewModel.getOperationList().observe(this, { checkList ->
            if (checkList.isNullOrEmpty()) {
                // если список пуст, скрываем заголовок списка мероприятий
                binding.operationListTitleTextView.visibility = View.GONE
            } else {
                binding.operationListTitleTextView.visibility = View.VISIBLE
            }
            operationListAdapter.submitList(checkList)
            Journal.insertJournal("OperationActivity->loadOperationList", list = checkList)
        })
    }

    /**
     * Отображение или скрытие значения таймера выполнения мероприятия взависимости от статуса мероприятия
     */
    private fun setTimer(eventState: Int) {
        Journal.insertJournal("OperationActivity->setTimer", eventState)
        var timerDuration = currentEvent?.durationTimer ?: 0
        when (eventState) {
            NEW -> {
                binding.timerImageView.visibility = View.GONE
                binding.timerValueTextView.visibility = View.GONE
                timerDuration = -1
            }
            IN_WORK -> {
                binding.timerImageView.visibility = View.VISIBLE
                binding.timerValueTextView.visibility = View.VISIBLE

                val dateTime = DateTimeUtil.getUnixDateTimeNow()
                val duration = currentEvent?.durationTimer ?: 0
                val delta = dateTime - (currentEvent?.dateTimeStartTimer ?: 0)
                timerDuration = duration + delta
                viewModel.startTimer()
            }
            PAUSED -> {
                binding.timerImageView.visibility = View.VISIBLE
                binding.timerValueTextView.visibility = View.VISIBLE
            }
            COMPLETED -> {
                binding.timerImageView.visibility = View.VISIBLE
                binding.timerValueTextView.visibility = View.VISIBLE
            }
            CANCELED -> {
                binding.timerImageView.visibility = View.INVISIBLE
                binding.timerValueTextView.visibility = View.INVISIBLE
            }
        }
        viewModel.setTimerValue(timerDuration)
    }

    /**
     * Отображение кнопок управления мероприятием взависимости от текущего статуса мероприятия
     */
    private fun configureControlButtonVisibility(clicked: Boolean) {
        if (clicked) {
            when (currentEventStatus) {
                NEW -> {
                    binding.startEventFab.show()
                    binding.startEventTextView.visibility = View.VISIBLE

                    binding.cancelEventFab.show()
                    binding.cancelEventTextView.visibility = View.VISIBLE
                }
                IN_WORK -> {
                    binding.stopEventFab.show()
                    binding.stopEventTextView.text =
                        getString(R.string.operation_activity_pause_event_button_work_state)
                    binding.stopEventTextView.visibility = View.VISIBLE

                    binding.completeEventFab.show()
                    binding.completeEventTextView.visibility = View.VISIBLE

                    binding.cancelEventFab.show()
                    binding.cancelEventTextView.visibility = View.VISIBLE
                }
                PAUSED -> {
                    binding.stopEventFab.show()
                    binding.stopEventTextView.text =
                        getString(R.string.operation_activity_pause_event_button_pause_state)
                    binding.stopEventTextView.visibility = View.VISIBLE

                    binding.cancelEventFab.show()
                    binding.cancelEventTextView.visibility = View.VISIBLE
                }
            }

            binding.eventControlFab.extend()
            binding.shadowView.visibility = View.VISIBLE
        } else {
            when (currentEventStatus) {
                COMPLETED, CANCELED -> {
                    binding.eventControlFab.visibility = View.GONE
                }
            }

            binding.startEventFab.hide()
            binding.stopEventFab.hide()
            binding.cancelEventFab.hide()
            binding.completeEventFab.hide()

            binding.startEventTextView.visibility = View.GONE
            binding.stopEventTextView.visibility = View.GONE
            binding.cancelEventTextView.visibility = View.GONE
            binding.completeEventTextView.visibility = View.GONE

            binding.eventControlFab.shrink()
            binding.shadowView.visibility = View.GONE
        }
    }

    /**
     * Установка значений параметров мероприятия
     */
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

        fillFactDate(event)
        fillEventComment(event)
    }

    /**
     * Установка актуальности тэга оборудования мероприятия
     */
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
        }
    }

    /**
     * Отображение статуса мероприятия
     */
    private fun fillOperationStatus(event: EventItem) {

        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(RoundedCornerTreatment())
            .setAllCornerSizes(RelativeCornerSize(0.5f))
            .build()

        val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        shapeDrawable.fillColor = ContextCompat.getColorStateList(this, R.color.md_white)
        binding.statusTextView.setTextColor(ContextCompat.getColor(this, R.color.md_white))

        when (event.status) {
            NEW -> {
                binding.statusTextView.text = "Можно выполнить"
                shapeDrawable.setStroke(2.0f, ContextCompat.getColor(this, R.color.md_grey_600))
                binding.statusTextView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.md_grey_600
                    )
                )
                binding.eventColorStatusView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.md_white
                    )
                )
            }
            IN_WORK -> {
                binding.statusTextView.text = "Выполняется"
                shapeDrawable.setStroke(2.0f, ContextCompat.getColor(this, R.color.md_blue_500))
                binding.statusTextView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.md_blue_500
                    )
                )
                binding.eventColorStatusView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.md_blue_500
                    )
                )
            }
            PAUSED -> {
                binding.statusTextView.text = "Остановлено"
                shapeDrawable.setStroke(2.0f, ContextCompat.getColor(this, R.color.colorAccent))
                binding.statusTextView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorAccent
                    )
                )
                binding.eventColorStatusView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorAccent
                    )
                )
            }
            COMPLETED -> {
                binding.statusTextView.text = "Завершено"
                shapeDrawable.setStroke(2.0f, ContextCompat.getColor(this, R.color.colorPrimary))
                binding.statusTextView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorPrimary
                    )
                )
                binding.eventColorStatusView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorPrimary
                    )
                )
            }
            CANCELED -> {
                binding.statusTextView.text = "Отменено"
                shapeDrawable.setStroke(2.0f, ContextCompat.getColor(this, R.color.md_red_600))
                binding.statusTextView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.md_red_600
                    )
                )
                binding.eventColorStatusView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.md_red_600
                    )
                )
            }
        }
        ViewCompat.setBackground(binding.statusTextView, shapeDrawable)
    }

    /**
     * Заполнение текста комментария к мероприятию
     */
    private fun fillEventComment(event: EventItem) {

        binding.eventCommentLabelTextView.visibility = View.GONE
        binding.eventCommentTextView.visibility = View.GONE

        if ((event.status == COMPLETED || event.status == CANCELED) && (!event.comment.isNullOrEmpty())) {
            binding.eventCommentLabelTextView.visibility = View.VISIBLE
            binding.eventCommentTextView.visibility = View.VISIBLE
            binding.eventCommentTextView.text = event.comment
        }
    }

    /**
     * Заполнение даты фактического выполнения/отказа мероприятия
     */
    private fun fillFactDate(event: EventItem) {
        if (event.factDate.isNullOrEmpty()) {
            binding.factDateTextView.visibility = View.GONE
            binding.factDateLabelTextView.visibility = View.GONE
        } else {
            binding.factDateTextView.visibility = View.VISIBLE
            binding.factDateLabelTextView.visibility = View.VISIBLE

            val factDate = try {
                (event.factDate ?: "0").toLong()
            } catch (e: Exception) {
                0
            }

            binding.factDateTextView.text =
                DateTimeUtil.getDateTimeFromMili(factDate, "dd.MM.yyyy HH:mm")

            when (event.status) {
                IN_WORK -> binding.factDateLabelTextView.text =
                    getString(R.string.event_plan_date_label_in_work)
                PAUSED -> binding.factDateLabelTextView.text =
                    getString(R.string.event_fact_date_label_pause)
                COMPLETED -> binding.factDateLabelTextView.text =
                    getString(R.string.event_fact_date_label_complete)
                CANCELED -> binding.factDateLabelTextView.text =
                    getString(R.string.event_fact_date_label_cancel)
                else -> binding.factDateLabelTextView.text =
                    getString(R.string.event_plan_date_label)
            }
        }
    }

    /**
     * Отображение сокращенного или полного списка параметров мероприятия
     */
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
                    R.drawable.ic_baseline_keyboard_arrow_up_24dp
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
                    R.drawable.ic_baseline_keyboard_arrow_down_24dp
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

    /**
     * Настройка Toolbar
     */
    private fun setToolbar() {
        setSupportActionBar(binding.operationToolbar)
        binding.operationToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.md_black))
        binding.operationToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white))
        supportActionBar?.let {
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24dp)
            it.setDisplayHomeAsUpEnabled(true)
        }
        this.title = getString(R.string.operation_activity_title)
    }

    /**
     * Завершение мероприятия
     * @param status - статус, с которым завершается мероприятие (COMPLETED или CANCELED)
     * @param comment - текст введеного комментария при завершении мероприятия
     */
    private fun finishEvent(status: Int, comment: String? = null) {
        Journal.insertJournal("OperationActivity->finishEvent", "status: $status, comment: $comment")

        viewModel.setDateTimeTimer(false)
        viewModel.stopTimer()
        setTimer(status)
        viewModel.setEventStatus(status, comment)
    }

    /**
     * Установка мероприятия на паузу или снятие мероприятия с паузы
     */
    private fun pauseEvent() {
        Journal.insertJournal("OperationActivity->pauseEvent", currentEventStatus)

        if (currentEventStatus == IN_WORK) {
            viewModel.setDateTimeTimer(false)
            viewModel.stopTimer()
            viewModel.setEventStatus(PAUSED)
            setTimer(PAUSED)
            showToast(getString(R.string.operation_activity_stop_event_message))
            finish()
        } else if (currentEventStatus == PAUSED) {
            viewModel.setDateTimeTimer(true)
            viewModel.startTimer()
            viewModel.setEventStatus(IN_WORK)
            setTimer(IN_WORK)
        }
    }

    /**
     * Старт выполнения мероприятия
     */
    private fun beginEvent() {

        Journal.insertJournal("OperationActivity->beginEvent", "")
        val deviceId = Util.getDeviceUniqueID(this)

        Util.deviceUniqueIdArray.forEach {
            if (it.equals(deviceId, true)) {
                needVerify = false
                return@forEach
            }
        }

        if (needVerify) {
            showToast(getString(R.string.operation_activity_start_event_error))
            showScanner()
        } else {
            viewModel.setDateTimeTimer(false)
            viewModel.startTimer()
            viewModel.setEventStatus(IN_WORK)
        }
    }

    /**
     * Проверка приоритета мероприятия и его запуск
     */
    private fun startEvent() {
        val currentPriority = currentEvent?.priority ?: 0 // приоритет текущего меропрития
        val currentEventId = currentEvent?.opId ?: 0 // id текущего мероприятия
        val isAccidentExists =
            viewModel.isAccidentPriorityEventsExists() // проверка на наличие аварий
        val isSeriousExists =
            viewModel.isSeriousPriorityEventsExists() // проверка на наличие важных мероприятий
        val isHighPriorityLaunched =
            viewModel.isHighPriorityEventsLaunched() // проверка на наличие запущенного высокоприоритетного мероприятия

        Journal.insertJournal(
            "OperationActivity->startEvent",
            "event: ${currentEvent}, isAccidentExists: ${isAccidentExists}, isSeriousExists: ${isSeriousExists}, isHighPriorityLaunched: ${isHighPriorityLaunched}"
        )

        // если имеется начатое высокоприоритетное мероприятие
        if (isHighPriorityLaunched) {

            val selectEventFragment = SelectEventFragment.newInstanceGetLaunchedHighPriorityEvent()
            selectEventFragment.show(
                supportFragmentManager,
                SelectEventFragment.SELECT_EVENT_FRAGMENT
            )
            selectEventFragment.onCloseListener = { finish() }

            // если текущее обычное мероприятие и есть высокоприоритетные, ожидающие выполнение
            // или текущее важное и есть аварийные
        } else if ((currentPriority == EventPriority.PLANED.ordinal && (isAccidentExists || isSeriousExists)) ||
            (currentPriority == EventPriority.SERIOUS.ordinal && isAccidentExists)
        ) {

            val selectEventFragment =
                SelectEventFragment.newInstanceGetHighPriorityEvents(currentEventId)
            selectEventFragment.show(
                supportFragmentManager,
                SelectEventFragment.SELECT_EVENT_FRAGMENT
            )
            selectEventFragment.onCloseListener = { finish() }
            showToast(getString(R.string.operation_activity_priority_check_message))

            // если текущее с самым высоким приоритетом
        } else {
            Journal.insertJournal(
                "OperationActivity->startEvent",
                "High Priority"
            )
            beginEvent()
            viewModel.changeControlButtonVisibleValue()
        }
    }

    /**
     * Отображение списка приоритетной информации по оборудованию, при наличии
     */
    private fun showEquipInfo(equip: EquipItem) {
        if (viewModel.isNotCheckedEquipInfoExists(equip.equipId) && !equipInfoIsShowing) {
            val equipInfoFragment = EquipInfoFragment.newInstance(equip)
            equipInfoFragment.show(
                supportFragmentManager,
                EquipInfoFragment.EQUIP_INFO_FRAGMENT_TAG
            )
            equipInfoIsShowing = true
        }
    }

    /**
     * Отображение сканера
     */
    private fun showScanner() {
        Journal.insertJournal("OperationActivity->showScanner", "")
        val scanner = NfcFragment.newInstanceStartEvent()
        scanner.show(supportFragmentManager, NfcFragment.NFC_FRAGMENT_TAG)
        scanner.onRFIDReadListener = {
            Journal.insertJournal("OperationActivity->showScanner->rfid", it)
            if (currentEvent?.equipRfid.equals(it, true)) {
                needVerify = false
                beginEvent()
            } else {
                showToast(getString(R.string.operation_activity_start_event_equip_error))
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun parseIntent() {
        if (!intent.hasExtra(EVENT_ID) || (!intent.hasExtra(NEED_VERIFY_FOR_BEGIN))) {
            finish()
            return
        }

        eventId = intent.getLongExtra(EVENT_ID, 0)
        needVerify = intent.getBooleanExtra(NEED_VERIFY_FOR_BEGIN, true)
        Journal.insertJournal("OperationActivity->parseIntent", "eventId: $eventId, needVerify: $needVerify")
    }

    companion object {

        private const val EVENT_ID = "event_item"
        private const val NEED_VERIFY_FOR_BEGIN = "need_verify_for_begin"

        fun newIntent(context: Context, eventId: Long, needVerify: Boolean): Intent {
            val intent = Intent(context, OperationActivity::class.java)
            intent.putExtra(EVENT_ID, eventId)
            intent.putExtra(NEED_VERIFY_FOR_BEGIN, needVerify)
            return intent
        }
    }
}