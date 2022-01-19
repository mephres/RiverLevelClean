package com.intas.metrolog.ui.requests.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.intas.metrolog.R
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.ui.requests.callback.RequestItemDiffCallback
import com.intas.metrolog.util.DateTimeUtil
import java.lang.String

class RequestListAdapter :
    ListAdapter<RequestItem, RequestItemViewHolder>(RequestItemDiffCallback()) {

    lateinit var db: AppDatabase
    lateinit var context: Context
    var onRequestLongClickListener: ((RequestItem) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        context = parent.context
        db = AppDatabase.getInstance(context)
        return RequestItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestItemViewHolder, position: Int) {

        val requestItem = getItem(position)

        val status = db.requestStatusDao().getRequestStatusById(requestItem.status)
        val discipline = db.disciplineDao().getDisciplineById(requestItem.discipline)
        val operation = db.eventOperationDao().getEventOperationById(requestItem.operationType)
        val equip = db.equipDao().getEquipItemById(requestItem.equipId.toLong())
        val sender = db.userDao().getUserById(requestItem.senderId)
        val executor = db.userDao().getUserById(requestItem.executorId?.toInt() ?: 0)

        holder.requestDateTextView.text =
            DateTimeUtil.getShortDataFromMili(requestItem.creationDate)

        holder.requestStatusTextView.text = status?.name ?: "Нет данных"

        if (!requestItem.comment.isNullOrEmpty()) {
            holder.requestCommentTextView.text = requestItem.comment
            holder.requestCommentTextView.visibility = View.VISIBLE
        } else {
            holder.requestCommentTextView.visibility = View.GONE
        }

        if (discipline != null) {
            val eventOperationString = operation?.name ?: "Тип операции не определен"
            val disciplineOperation =
                String.format("%s. %s", discipline.name, eventOperationString)
            holder.requestDisciplineOperationTextView.text = disciplineOperation
        } else {
            holder.requestDisciplineOperationTextView.text = "Дисциплина не определена"
        }

        if (equip != null) {
            val titleString = String.format(
                "%s [%s] - %s",
                equip.equipName,
                equip.equipTag,
                equip.mestUstan
            )
            holder.requestTitleTextView.text = titleString
        } else {
            holder.requestTitleTextView.text = "Оборудование не найдено"
        }

        if (sender != null) {
            holder.requestFromTextView.text = sender.fio
        } else {
            holder.requestFromTextView.text = "Нет данных"
        }

        if (executor != null) {
            holder.requestExecutorTextView.text = executor.fio
            holder.requestExecutorTextView.visibility = View.VISIBLE
            holder.requestExecutorLabelTextView.visibility = View.VISIBLE
        } else {
            holder.requestExecutorTextView.visibility = View.GONE
            holder.requestExecutorLabelTextView.visibility = View.GONE
        }

        when (status?.id) {
            1 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_yellow_600))
            }
            2 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_green_600))
            }
            3 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_blue_A700))
            }
            4 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_grey_500))
            }
            5 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_red_600))
            }
            6 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_white_1000))
            }
        }

        holder.itemView.setOnLongClickListener {
            onRequestLongClickListener?.invoke(requestItem)
            true
        }
    }

    override fun onViewRecycled(holder: RequestItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }


}