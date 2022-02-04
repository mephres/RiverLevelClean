package com.intas.metrolog.ui.chat.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R

class ChatItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val chatUserNameTextView: TextView = view.findViewById(R.id.chatUserNameTextView)
    val chatMessageTextTextView: TextView = view.findViewById(R.id.chatMessageTextTextView)
    val chatUserPositionTextView: TextView = view.findViewById(R.id.chatUserPositionTextView)
    val chatIncomingMessagesCountTextView: TextView = view.findViewById(R.id.chatIncomingMessagesCountTextView)
    val chatItemMessageDateTimeTextView: TextView = view.findViewById(R.id.chatItemMessageDateTimeTextView)
    val chatUserPhotoImageView: ImageView = view.findViewById(R.id.chatUserPhotoImageView)
}
