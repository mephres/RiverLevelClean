package com.intas.metrolog.ui.requests.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.request.RequestItem

class RequestItemDiffCallback: DiffUtil.ItemCallback<RequestItem>() {
    override fun areItemsTheSame(oldItem: RequestItem, newItem: RequestItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RequestItem, newItem: RequestItem): Boolean {
        return oldItem == newItem
    }
}
