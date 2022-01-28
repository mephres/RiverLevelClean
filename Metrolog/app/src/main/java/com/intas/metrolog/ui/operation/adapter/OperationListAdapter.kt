package com.intas.metrolog.ui.operation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.intas.metrolog.R
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import com.intas.metrolog.ui.operation.adapter.callback.EventOperationItemDiffCallback

class OperationListAdapter : ListAdapter<EventOperationItem, OperationItemViewHolder>(
    EventOperationItemDiffCallback()
) {

    lateinit var context: Context
    var onItemClickListener: ((EventOperationItem) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_operation_item, parent, false)
        context = parent.context
        return OperationItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperationItemViewHolder, position: Int) {

        val eventItem = getItem(position)

        holder.checkListNameTextView.text = eventItem.subName
        holder.manHourNormTextView.text = eventItem.subManhour.toString()

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(eventItem)
        }
    }

    override fun onViewRecycled(holder: OperationItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}