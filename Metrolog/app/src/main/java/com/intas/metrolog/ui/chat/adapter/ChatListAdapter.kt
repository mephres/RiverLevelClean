package com.intas.metrolog.ui.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.intas.metrolog.R
import com.intas.metrolog.pojo.chat.ChatItem
import com.intas.metrolog.ui.chat.adapter.callback.ChatItemDiffCallback
import com.intas.metrolog.util.DateTimeUtil

class ChatListAdapter : ListAdapter<ChatItem, ChatItemViewHolder>(ChatItemDiffCallback()) {

    lateinit var context: Context
    var onChatItemClickListener: ((ChatItem) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        context = parent.context
        return ChatItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {

        val chatItem = getItem(position)

        holder.chatUserNameTextView.text = chatItem.companion.fio
        holder.chatMessageTextTextView.text = chatItem.lastMessage
        holder.chatItemMessageDateTimeTextView.text = DateTimeUtil.getShortDataFromMili(chatItem.lastMessageDate)
        holder.chatUserPositionTextView.text = chatItem.companion.position

        Glide.with(context).load(R.drawable.ic_worker).circleCrop()
            .into(holder.chatUserPhotoImageView)

        if (chatItem.notViewedMessageCount > 0) {
            holder.chatIncomingMessagesCountTextView.text = chatItem.notViewedMessageCount.toString()
            holder.chatIncomingMessagesCountTextView.visibility = View.VISIBLE
        } else {
            holder.chatIncomingMessagesCountTextView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onChatItemClickListener?.invoke(chatItem)
        }
    }

    override fun onViewRecycled(holder: ChatItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}