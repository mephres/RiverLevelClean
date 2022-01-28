package com.intas.metrolog.ui.operation.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R

class OperationItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val checkListNameTextView: TextView = view.findViewById(R.id.checkListNameTextView)
    val manHourNormTextView: TextView = view.findViewById(R.id.manHourNormTextView)

}