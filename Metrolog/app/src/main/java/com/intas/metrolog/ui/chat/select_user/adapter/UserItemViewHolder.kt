package com.intas.metrolog.ui.chat.select_user.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R

class UserItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val chatUserItemFullNameTextView: TextView = view.findViewById(R.id.chatUserItemFullNameTextView)
    val chatUserItemPositionTextView: TextView = view.findViewById(R.id.chatUserItemPositionTextView)
    val chatUserPhotoImageView: ImageView = view.findViewById(R.id.chatUserPhotoImageView)
}
