package com.intas.metrolog.ui.operation.equip_info.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R

class EquipInfoItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val equipInfoTextView: TextView = view.findViewById(R.id.equipInfoTextView)
    val equipInfoCheckBox: CheckBox = view.findViewById(R.id.equipInfoCheckBox)
}
