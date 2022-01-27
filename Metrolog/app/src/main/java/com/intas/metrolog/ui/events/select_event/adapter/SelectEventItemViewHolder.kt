package com.intas.metrolog.ui.events.select_event.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R

class SelectEventItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val eventNameTextView: TextView = view.findViewById(R.id.eventNameTextView)
    val planDateTextView: TextView = view.findViewById(R.id.planDateTextView)
    val eventTypeTextView: TextView = view.findViewById(R.id.eventTypeTextView)
    val eventStatusImageView: ImageView = view.findViewById(R.id.eventStatusImageView)
    val eventItemContainer: View = view.findViewById(R.id.eventItemContainer)
}