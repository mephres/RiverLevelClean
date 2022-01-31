package com.intas.metrolog.ui.operation.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.intas.metrolog.R

class OperationItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val checkListCardView: MaterialCardView = view.findViewById(R.id.checkListCardView)
    val checkListNameTextView: TextView = view.findViewById(R.id.checkListNameTextView)
    val equipInfoTextView: TextView = view.findViewById(R.id.equipInfoTextView)
    val manHourNormLabelTextView: TextView = view.findViewById(R.id.manHourNormLabelTextView)
    val manHourNormTextView: TextView = view.findViewById(R.id.manHourNormTextView)
    val completedDateTimeLabelTextView: TextView = view.findViewById(R.id.completedDateTimeLabelTextView)
    val completedDateTimeTextView: TextView = view.findViewById(R.id.completedDateTimeTextView)
    val completedUserLabelTextView: TextView = view.findViewById(R.id.completedUserLabelTextView)
    val completedUserTextView: TextView = view.findViewById(R.id.completedUserTextView)

}