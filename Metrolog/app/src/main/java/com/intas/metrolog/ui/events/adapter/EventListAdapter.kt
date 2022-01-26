package com.intas.metrolog.ui.events.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.intas.metrolog.R
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.ui.events.adapter.callback.EventItemDiffCallback
import com.intas.metrolog.util.DateTimeUtil

class EventListAdapter : ListAdapter<EventItem, EventItemViewHolder>(EventItemDiffCallback()) {

    lateinit var context: Context
    lateinit var db: AppDatabase
    var onEventClickListener: ((EventItem) -> Unit)? = null

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

        holder.equipNameTextView.text = eventItem.comment
        holder.equipZavNumTextView.text =
            equipItem.equipZavNum ?: context.getString(R.string.no_data)
        holder.equipRFIDTextView.text = eventItem.equipRfid ?: context.getString(R.string.no_data)
        holder.equipTagTextView.text = equipItem.equipTag ?: context.getString(R.string.no_data)
        holder.equipLocationTextView.text =
            equipItem.mestUstan ?: context.getString(R.string.no_data)
        holder.equipGRSITextView.text = equipItem.equipGRSI ?: context.getString(R.string.no_data)
        holder.equipManufacturerTextView.text =
            equipItem.equipZavodIzg ?: context.getString(R.string.no_data)
        holder.equipMeteringTypeTextView.text =
            equipItem.equipVidIzm ?: context.getString(R.string.no_data)
        holder.equipCalibrationTextView.text =
            equipItem.lastCalibr ?: context.getString(R.string.no_data)
        holder.equipVerificationTextView.text =
            equipItem.lastVerif ?: context.getString(R.string.no_data)
        holder.eventNameTextView.text = eventItem.name ?: context.getString(R.string.no_data)
        holder.checkListSizeTextView.text = eventItem.operationListSize.toString()
        holder.typeTextView.text = "Плановое"
        if (eventItem.unscheduled != 0) {
            holder.typeTextView.text = "Внеплановое"
        }
        holder.planDateTextView.text = DateTimeUtil.getShortDataFromMili(eventItem.planDate ?: 0)
        holder.factDateTextView.text =
            DateTimeUtil.getDateTimeFromMili((eventItem.factDate ?: "0").toLong(), "dd.MM.yyyy HH:mm")

        /*
        holder.equipNameTextView.text =
        holder.equipZavNumLabelTextView.text =
        holder.equipZavNumTextView.text =
        holder.equipRFIDLabelTextView.text =
        holder.equipRFIDTextView.text =
        holder.equipTagLabelTextView.text =
        holder.equipTagTextView.text =
        holder.equipLocationLabelTextView.text =
        holder.equipLocationTextView.text =
        holder.equipGRSILabelTextView.text =
        holder.equipGRSITextView.text =
        holder.equipManufacturerLabelTextView.text =
        holder.equipManufacturerTextView.text =
        holder.equipMeteringTypeLabelTextView.text =
        holder.equipMeteringTypeTextView.text =
        holder.equipCalibrationLabelTextView.text =
        holder.equipCalibrationTextView.text =
        holder.equipVerificationLabelTextView.text =
        holder.equipVerificationTextView.text =
        holder.eventNameTextView.text =
        holder.checkListSizeTextView.text =
        holder.typeLabelTextView.text =
        holder.typeTextView.text =
        holder.planDateLabelTextView.text =
        holder.planDateTextView.text =
        holder.factDateLabelTextView.text =
        holder.factDateTextView.text =      */


        holder.eventCardView.setOnClickListener {
            onEventClickListener?.invoke(eventItem)
        }
    }

    override fun onViewRecycled(holder: EventItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}