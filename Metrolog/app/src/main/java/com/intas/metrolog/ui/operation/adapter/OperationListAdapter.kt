package com.intas.metrolog.ui.operation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.intas.metrolog.R
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import com.intas.metrolog.ui.operation.adapter.callback.EventOperationItemDiffCallback
import com.intas.metrolog.util.DateTimeUtil

class OperationListAdapter : ListAdapter<EventOperationItem, OperationItemViewHolder>(
    EventOperationItemDiffCallback()
) {
    lateinit var db: AppDatabase
    lateinit var context: Context
    var onItemClickListener: ((EventOperationItem) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationItemViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_operation_item, parent, false)
        context = parent.context
        db = AppDatabase.getInstance(context)
        return OperationItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperationItemViewHolder, position: Int) {

        val operationItem = getItem(position)
        holder.completedUserTextView.visibility = View.GONE
        holder.completedDateTimeTextView.visibility = View.GONE
        holder.completedDateTimeLabelTextView.visibility = View.GONE
        holder.completedUserLabelTextView.visibility = View.GONE
        holder.equipInfoTextView.visibility = View.GONE

        if (operationItem.completed > 0) {
            holder.checkListCardView.strokeColor = ContextCompat.getColor(
                context,
                R.color.colorPrimary
            )
            holder.completedUserTextView.visibility = View.VISIBLE
            holder.completedDateTimeTextView.visibility = View.VISIBLE
            holder.completedDateTimeLabelTextView.visibility = View.VISIBLE
            holder.completedUserLabelTextView.visibility = View.VISIBLE
            holder.completedUserLabelTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_white_1000
                )
            )
            holder.completedUserTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_grey_300
                )
            )
            holder.completedDateTimeLabelTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_white_1000
                )
            )
            holder.completedDateTimeTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_grey_300
                )
            )

            val completedUser = db.userDao().getUserById(operationItem.completedUserId.toInt())

            holder.completedUserTextView.text = completedUser?.fio
            holder.completedDateTimeTextView.text = DateTimeUtil.getLongDateFromMili(
                operationItem.dateEnd
            )

            holder.checkListNameTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_white_1000
                )
            )
            holder.equipInfoTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_grey_300
                )
            )
            holder.manHourNormLabelTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_white_1000
                )
            )
            holder.manHourNormTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_grey_300
                )
            )
        } else {
            holder.checkListCardView.strokeColor = ContextCompat.getColor(
                context,
                R.color.md_white_1000
            )
        }

        holder.checkListNameTextView.text = operationItem.subName
        holder.manHourNormTextView.text =
            operationItem.subManhour?.toString() ?: context.getString(R.string.no_data)

        if (operationItem.equipId > 0) {
            val equip = db.equipDao().getEquipItemById(operationItem.equipId)
            holder.equipInfoTextView.visibility = View.VISIBLE
            holder.equipInfoTextView.text = equip?.equipName
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(operationItem)
        }
    }

    override fun onViewRecycled(holder: OperationItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}