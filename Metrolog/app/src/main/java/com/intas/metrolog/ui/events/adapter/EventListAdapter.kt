package com.intas.metrolog.ui.events.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import com.intas.metrolog.R
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.CANCELED
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.COMPLETED
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.IN_WORK
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.PAUSED
import com.intas.metrolog.ui.events.adapter.callback.EventItemDiffCallback
import com.intas.metrolog.util.DateTimeUtil

class EventListAdapter : ListAdapter<EventItem, EventItemViewHolder>(EventItemDiffCallback()) {

    lateinit var context: Context
    lateinit var db: AppDatabase
    var onEventClickListener: ((EventItem) -> Unit)? = null

    val enterTransition = MaterialFadeThrough()
    val exitTransition = MaterialFadeThrough()

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        context = parent.context
        db = AppDatabase.getInstance(context)
        return EventItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {

        val eventItem = getItem(position)
        val equipItem = db.equipDao().getEquipItemById(eventItem.equipId ?: 0) ?: return

        holder.equipNameTextView.text = eventItem.equipName
        holder.equipZavNumTextView.text =  if (!equipItem.equipZavNum.isNullOrEmpty()) equipItem.equipZavNum else context.getString(R.string.no_data)

        fillRFID(holder, eventItem)

        holder.equipTagTextView.text = if (!equipItem.equipTag.isNullOrEmpty()) equipItem.equipTag else context.getString(R.string.no_data)
        holder.equipLocationTextView.text = if (!equipItem.mestUstan.isNullOrEmpty()) equipItem.mestUstan else context.getString(R.string.no_data)
        holder.equipGRSITextView.text = if (!equipItem.equipGRSI.isNullOrEmpty()) equipItem.equipGRSI else context.getString(R.string.no_data)
        holder.equipManufacturerTextView.text = if (!equipItem.equipZavodIzg.isNullOrEmpty()) equipItem.equipZavodIzg else context.getString(R.string.no_data)
        holder.equipMeteringTypeTextView.text = if (!equipItem.equipVidIzm.isNullOrEmpty()) equipItem.equipVidIzm else context.getString(R.string.no_data)
        holder.equipCalibrationTextView.text = if (!equipItem.lastCalibr.isNullOrEmpty()) equipItem.lastCalibr else context.getString(R.string.no_data)
        holder.equipVerificationTextView.text = if (!equipItem.lastVerif.isNullOrEmpty()) equipItem.lastVerif else context.getString(R.string.no_data)

        holder.eventNameTextView.text = eventItem.name ?: context.getString(R.string.no_data)

        fillOperationCount(holder, eventItem)

        holder.typeTextView.text = "Плановое"
        if (eventItem.unscheduled != 0) {
            holder.typeTextView.text = "Внеплановое"
        }
        holder.planDateTextView.text = DateTimeUtil.getDateTimeFromMili(eventItem.planDate ?: 0, "dd.MM.yyyy")

        fillFactDate(eventItem, holder)

        fillTagActual(equipItem, holder)

        holder.itemView.setOnClickListener {
            onEventClickListener?.invoke(eventItem)
        }

        holder.equipFullInfoImageView.setOnClickListener {
            changeEventInfoValue(holder)
        }
    }

    private fun fillRFID(
        holder: EventItemViewHolder,
        eventItem: EventItem
    ) {
        if (!eventItem.equipRfid.isNullOrEmpty()) {
            holder.equipRFIDTextView.text = eventItem.equipRfid
            holder.equipLabelImageView.visibility = View.VISIBLE
        } else {
            holder.equipRFIDTextView.text = context.getString(R.string.no_data)
            holder.equipLabelImageView.visibility = View.GONE
        }
    }

    private fun fillTagActual(
        equipItem: EquipItem,
        holder: EventItemViewHolder
    ) {
        when (equipItem.equipTagActual) {
            0 -> {
                holder.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_baseline_cancel_black_24dp
                    )
                )
                holder.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.md_red_600
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
            1 -> {
                holder.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_baseline_check_circle_black_24dp
                    )
                )
                holder.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.colorAccent
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
            else -> {
                holder.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_warning_red_24dp
                    )
                )
                holder.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.md_deep_orange_A200
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
        }
    }

    private fun changeEventInfoValue(holder: EventItemViewHolder) {

        TransitionManager.beginDelayedTransition(
            (holder.itemView.getRootView() as ViewGroup),
            enterTransition
        )

        holder.isFullInfo = !holder.isFullInfo

        if ( holder.isFullInfo) {

            holder.equipRFIDTextView.visibility = View.VISIBLE
            holder.equipRFIDLabelTextView.visibility = View.VISIBLE
            holder.equipGRSITextView.visibility = View.VISIBLE
            holder.equipGRSILabelTextView.visibility = View.VISIBLE
            holder.equipManufacturerTextView.visibility = View.VISIBLE
            holder.equipManufacturerLabelTextView.visibility = View.VISIBLE
            holder.equipMeteringTypeTextView.visibility = View.VISIBLE
            holder.equipMeteringTypeLabelTextView.visibility = View.VISIBLE
            holder.equipCalibrationTextView.visibility = View.VISIBLE
            holder.equipCalibrationLabelTextView.visibility = View.VISIBLE
            holder.equipVerificationTextView.visibility = View.VISIBLE
            holder.equipVerificationLabelTextView.visibility = View.VISIBLE

            holder.equipFullInfoImageView.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24dp))
        } else {

            holder.equipRFIDTextView.visibility = View.GONE
            holder.equipRFIDLabelTextView.visibility = View.GONE
            holder.equipGRSITextView.visibility = View.GONE
            holder.equipGRSILabelTextView.visibility = View.GONE
            holder.equipManufacturerTextView.visibility = View.GONE
            holder.equipManufacturerLabelTextView.visibility = View.GONE
            holder.equipMeteringTypeTextView.visibility = View.GONE
            holder.equipMeteringTypeLabelTextView.visibility = View.GONE
            holder.equipCalibrationTextView.visibility = View.GONE
            holder.equipCalibrationLabelTextView.visibility = View.GONE
            holder.equipVerificationTextView.visibility = View.GONE
            holder.equipVerificationLabelTextView.visibility = View.GONE

            holder.equipFullInfoImageView.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24dp))
        }
    }

    private fun fillOperationCount(
        holder: EventItemViewHolder,
        eventItem: EventItem
    ) {
        holder.checkListSizeTextView.text = eventItem.operationListSize.toString()
        holder.checkListSizeTextView.setVisibility(if (eventItem.operationListSize > 0) View.VISIBLE else View.INVISIBLE)
    }

    private fun fillFactDate(
        eventItem: EventItem,
        holder: EventItemViewHolder
    ) {
        if (eventItem.factDate.isNullOrEmpty()) {
            holder.factDateTextView.visibility = View.GONE
            holder.factDateLabelTextView.visibility = View.GONE
        } else {
            holder.factDateTextView.visibility = View.VISIBLE
            holder.factDateLabelTextView.visibility = View.VISIBLE

            val factDate = try {
                (eventItem.factDate).toLong()
            } catch (e: Exception) {
                0
            }

            holder.factDateTextView.text =
                DateTimeUtil.getDateTimeFromMili(factDate, "dd.MM.yyyy HH:mm")

            when (eventItem.status) {
                IN_WORK -> holder.factDateLabelTextView.text =
                    context.getString(R.string.event_plan_date_label_in_work)
                PAUSED -> holder.factDateLabelTextView.text =
                    context.getString(R.string.event_fact_date_label_pause)
                COMPLETED -> holder.factDateLabelTextView.text =
                    context.getString(R.string.event_fact_date_label_complete)
                CANCELED -> holder.factDateLabelTextView.text =
                    context.getString(R.string.event_fact_date_label_cancel)
                else -> holder.factDateLabelTextView.setText(context.getString(R.string.event_plan_date_label))
            }
        }
    }

    override fun onViewRecycled(holder: EventItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}