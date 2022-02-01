package com.intas.metrolog.ui.chat.messages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.intas.metrolog.R
import com.intas.metrolog.pojo.chat.MessageItem
import com.intas.metrolog.ui.chat.messages.adapter.callback.MessageItemDiffCallback
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util
import com.intas.metrolog.util.ViewUtil
import java.util.*

class MessageListAdapter :
    ListAdapter<MessageItem, MessageItemViewHolder>(MessageItemDiffCallback()) {

    lateinit var context: Context
    var textSize = 14F

    var onMessageItemLongClickListener: ((View, MessageItem) -> Unit)? = null
    var onMessageItemForwardClickListener: ((View, MessageItem) -> Unit)? = null
    var onMessageItemClickListener: ((View, MessageItem) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
        private const val MSG_TYPE_LEFT = 0
        private const val MSG_TYPE_RIGHT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder {

        context = parent.context

        textSize = PreferenceManager.getDefaultSharedPreferences(context).getInt("message_text_size", 14).toFloat()

        return if (viewType == MSG_TYPE_RIGHT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_message_item_right, parent, false)
            MessageItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_message_item_left, parent, false)
            MessageItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {

        val messageItem = getItem(position)

        var previousDateTime = 0L
        if (position > 0)  {
            val previousMessage = getItem(position - 1)
            previousDateTime = previousMessage.dateTime ?: 0
        }

        if (previousDateTime == 0L) {
            holder.messageGroupDateTextView.visibility = View.VISIBLE
            holder.messageGroupDateTextView.text = DateTimeUtil.getChatMessageDateTime(messageItem.dateTime ?: 0, 1)
        } else {
            val currentCalendar = Calendar.getInstance()
            val previousCalendar = Calendar.getInstance()
            currentCalendar.setTimeInMillis((messageItem.dateTime ?: 0) * 1000)
            previousCalendar.setTimeInMillis(previousDateTime * 1000)

            val sameDay = currentCalendar.get(Calendar.YEAR) === previousCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.DAY_OF_YEAR) === previousCalendar.get(Calendar.DAY_OF_YEAR)
            if (sameDay) {
                holder.messageGroupDateTextView.visibility = View.GONE
            } else {
                holder.messageGroupDateTextView.visibility = View.VISIBLE
                holder.messageGroupDateTextView.text = DateTimeUtil.getChatMessageDateTime(messageItem.dateTime ?: 0, 1)
            }
        }

        holder.forwardedTextMessageImageView.visibility = View.GONE
        holder.forwardedTextMessageTextView.visibility = View.GONE

        holder.showMessageTextView.text = messageItem.message
        holder.showMessageTextView.textSize = textSize

        holder.messageDateTextView.text = DateTimeUtil.getShortTimeFromMili(messageItem.dateTime ?: 0)

        if (messageItem.senderUserId == Util.authUser?.userId) {
            if (messageItem.isSent == 1) {
                Glide.with(context).load(R.drawable.ic_baseline_done_all_black_24dp).into(holder.messageStatusImageView)
                holder.messageStatusImageView.setColorFilter(ContextCompat.getColor(context, R.color.md_grey_600))
            } else {
                Glide.with(context).load(R.drawable.ic_baseline_done_black_24dp).into(holder.messageStatusImageView)
                holder.messageStatusImageView.setColorFilter(ContextCompat.getColor(context, R.color.md_grey_600))
            }
        }

        holder.forwardMessageImageView.visibility = View.GONE

        holder.itemView.setOnLongClickListener {
            onMessageItemLongClickListener?.invoke(it, messageItem)
            true
        }

        holder.forwardMessageImageView.setOnClickListener {

            ViewUtil.runAnimationButton(context, it)

            onMessageItemForwardClickListener?.invoke(it, messageItem)
        }

        holder.itemView.setOnClickListener {
            onMessageItemClickListener?.invoke(it, messageItem)
        }
    }

    override fun onViewRecycled(holder: MessageItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderUserId == Util.authUser?.userId) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }
}