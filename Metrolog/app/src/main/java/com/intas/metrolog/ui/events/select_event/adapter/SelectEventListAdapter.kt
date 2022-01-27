package com.intas.metrolog.ui.events.select_event.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.intas.metrolog.R
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_priority.EventPriority
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.ui.equip.callback.EquipItemDiffCallback
import com.intas.metrolog.ui.events.select_event.callback.SelectEventItemDiffCallback
import com.intas.metrolog.util.DateTimeUtil

class SelectEventListAdapter : ListAdapter<EventItem, SelectEventItemViewHolder>(SelectEventItemDiffCallback()) {

    lateinit var context: Context
    var onItemClickListener: ((EventItem) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectEventItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.select_event_item, parent, false)
        context = parent.context
        return SelectEventItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectEventItemViewHolder, position: Int) {

        val eventItem = getItem(position)
        holder.eventNameTextView.text = eventItem.name

        eventItem.planDate?.let {
            holder.planDateTextView.text = DateTimeUtil.getShortDataFromMili(it)
        }

        if (eventItem.unscheduled == 1) {
            holder.eventTypeTextView.text = context.getString(R.string.select_event_type_unscheduled_title)
        } else {
            holder.eventTypeTextView.text = context.getString(R.string.select_event_type_planned_title)
        }

        when(eventItem.priority) {
            EventPriority.PLANED.ordinal -> {
                holder.eventItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.md_white_1000))
            }
            EventPriority.ACCIDENT.ordinal -> {
                holder.eventItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.md_orange_300))
            }
            EventPriority.SERIOUS.ordinal -> {
                holder.eventItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.md_red_500))
            }
            EventPriority.UNKNOWN.ordinal -> {
                holder.eventItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.md_white_1000))
            }
        }

        when(eventItem.status) {
            EventStatus.NEW.ordinal -> {
                holder.eventStatusImageView.visibility = View.INVISIBLE
            }
            EventStatus.IN_WORK.ordinal -> {
                holder.eventStatusImageView.setImageResource(R.drawable.ic_timer_red_24dp)
            }
            EventStatus.PAUSED.ordinal -> {
                holder.eventStatusImageView.setImageResource(R.drawable.ic_timer_off_red_24dp)
            }
            EventStatus.COMPLETED.ordinal -> {
                holder.eventStatusImageView.setImageResource(R.drawable.ic_check_red_24dp)
            }
            EventStatus.CANCELED.ordinal -> {
                holder.eventStatusImageView.setImageResource(R.drawable.ic_close_red_24dp)
            }
        }


        onItemClickListener?.invoke(eventItem)
    }

    override fun onViewRecycled(holder: SelectEventItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}