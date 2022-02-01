package com.intas.metrolog.ui.chat.select_user.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.intas.metrolog.R
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.ui.chat.select_user.adapter.callback.UserItemDiffCallback

class UserListAdapter :
    ListAdapter<UserItem, UserItemViewHolder>(UserItemDiffCallback()) {

    lateinit var context: Context
    var onUserItemClickListener: ((UserItem) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserItemViewHolder(view)
    }


    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {

        val userItem = getItem(position)

        holder.chatUserItemFullNameTextView.text = userItem.fio
        holder.chatUserItemPositionTextView.text = userItem.position

        holder.itemView.setOnClickListener {
            onUserItemClickListener?.invoke(userItem)
        }

    }


    override fun onViewRecycled(holder: UserItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}