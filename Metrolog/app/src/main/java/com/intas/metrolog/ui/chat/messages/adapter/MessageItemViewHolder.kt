package com.intas.metrolog.ui.chat.messages.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.intas.metrolog.R

class MessageItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val showMessageTextView: TextView = view.findViewById(R.id.showMessageTextView)
    val messageDateTextView: TextView = view.findViewById(R.id.messageDateTextView)
    val messageGroupDateTextView: TextView = view.findViewById(R.id.messageGroupDateTextView)
    val messageStatusImageView: ImageView = view.findViewById(R.id.messageStatusImageView)
    val messageTextCardView: MaterialCardView = view.findViewById(R.id.messageTextCardView)
    val forwardedTextMessageTextView: TextView = view.findViewById(R.id.forwardedTextMessageTextView)
    val forwardedTextMessageImageView: ImageView = view.findViewById(R.id.forwardedTextMessageImageView)
}
