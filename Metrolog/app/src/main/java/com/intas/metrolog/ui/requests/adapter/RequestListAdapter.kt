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
        val operationType = db.eventOperationTypeDao().getEventOperationTypeById(requestItem.operationType)
        val sender = db.userDao().getUserById(requestItem.senderId)

        val executorId = try {
            requestItem.executorId?.toInt()
        } catch (e: Exception) {
            -1
        }
        val executor = db.userDao().getUserById(executorId ?: -1)

        val equipId = try {
            requestItem.equipId?.toLong()
        } catch (e: Exception) {
            -1
        }
        val equip = db.equipDao().getEquipItemById(equipId ?: -1)

        holder.requestDateTextView.text =
            DateTimeUtil.getShortDataFromMili(requestItem.creationDate)

        holder.requestStatusTextView.text = status?.name ?: context.getString(R.string.no_data)

        if (!requestItem.comment.isNullOrEmpty()) {
            holder.requestCommentTextView.text = requestItem.comment
            holder.requestCommentTextView.visibility = View.VISIBLE
        } else {
            holder.requestCommentTextView.visibility = View.GONE
        }

        if (discipline != null) {
            val eventOperationString = operationType?.name ?: context.getString(R.string.request_no_operation_type)
            val disciplineOperation =
                String.format("%s. %s", discipline.name, eventOperationString)
            holder.requestDisciplineOperationTextView.text = disciplineOperation
        } else {
            holder.requestDisciplineOperationTextView.text = context.getString(R.string.request_no_discipline_type)
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
            holder.requestTitleTextView.text = context.getString(R.string.request_no_equip)
        }

        if (sender != null) {
            holder.requestFromTextView.text = sender.fio
        } else {
            holder.requestFromTextView.text = context.getString(R.string.no_data)
        }

        if (executor != null) {
            holder.requestExecutorTextView.text = executor.fio
            holder.requestExecutorTextView.visibility = View.VISIBLE
            holder.requestExecutorLabelTextView.visibility = View.VISIBLE
        } else {
            holder.requestExecutorTextView.visibility = View.GONE
            holder.requestExecutorLabelTextView.visibility = View.GONE
        }

        when(requestItem.typeRequest) {
            0 -> {
                holder.requestTypeImageView.visibility = View.GONE
            }
            1 -> {
                holder.requestTypeImageView.visibility = View.VISIBLE
            }
            else -> {
                holder.requestTypeImageView.visibility = View.GONE
            }
        }

        when(requestItem.isSended) {
            0 -> {
                holder.requestIsNotSendImageView.visibility = ViewGroup.VISIBLE
            }
            1 -> {
                holder.requestIsNotSendImageView.visibility = ViewGroup.GONE
            }
            else -> {
                holder.requestTypeImageView.visibility = View.VISIBLE
            }
        }

        when (status?.id) {
            1 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_yellow_600))
            }
            2 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_green_600))
                holder.requestIsNotSendImageView.visibility = View.GONE
            }
            3 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_blue_A700))
                holder.requestIsNotSendImageView.visibility = View.GONE
            }
            4 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_grey_500))
                holder.requestIsNotSendImageView.visibility = View.GONE
            }
            5 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_red_600))
                holder.requestIsNotSendImageView.visibility = View.GONE
            }
            6 -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_white))
                holder.requestIsNotSendImageView.visibility = View.GONE
            }
            else -> {
                holder.requestColorStatusTextView.background
                    .setTint(ContextCompat.getColor(context, R.color.md_white))
                holder.requestIsNotSendImageView.visibility = View.VISIBLE
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