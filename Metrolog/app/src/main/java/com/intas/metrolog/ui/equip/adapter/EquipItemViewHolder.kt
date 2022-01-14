package com.intas.metrolog.ui.equip.adapter

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R

class EquipItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val equipTagActualImageView: ImageView = view.findViewById(R.id.equipTagActualImageView)
    val equipNameTextView: TextView = view.findViewById(R.id.equipNameTextView)
    val equipZavNumTextView: TextView = view.findViewById(R.id.equipZavNumTextView)
    val equipRFIDTextView: TextView = view.findViewById(R.id.equipRFIDTextView)
    val equipTagTextView: TextView = view.findViewById(R.id.equipTagTextView)
    val equipLocationTextView: TextView = view.findViewById(R.id.equipLocationTextView)
    val equipGRSITextView: TextView = view.findViewById(R.id.equipGRSITextView)
    val equipGRSILabelTextView: TextView = view.findViewById(R.id.equipGRSILabelTextView)
    val equipManufacturerTextView: TextView = view.findViewById(R.id.equipManufacturerTextView)
    val equipManufacturerLabelTextView: TextView = view.findViewById(R.id.equipManufacturerLabelTextView)
    val equipMeteringTypeTextView: TextView = view.findViewById(R.id.equipMeteringTypeTextView)
    val equipMeteringTypeLabelTextView: TextView = view.findViewById(R.id.equipMeteringTypeLabelTextView)
    val equipCalibrationTextView: TextView = view.findViewById(R.id.equipCalibrationTextView)
    val equipCalibrationLabelTextView: TextView = view.findViewById(R.id.equipCalibrationLabelTextView)
    val equipVerificationTextView: TextView = view.findViewById(R.id.equipVerificationTextView)
    val equipVerificationLabelTextView: TextView = view.findViewById(R.id.equipVerificationLabelTextView)
    val addRFIDButton: Button = view.findViewById(R.id.addRFIDButton)
    val createDocumentButton: Button = view.findViewById(R.id.createDocumentButton)
}
