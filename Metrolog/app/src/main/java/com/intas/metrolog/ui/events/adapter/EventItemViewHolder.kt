package com.intas.metrolog.ui.events.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.intas.metrolog.R

class EventItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val eventCardView: MaterialCardView = view.findViewById(R.id.eventCardView)
    val equipNameTextView: TextView = view.findViewById(R.id.equipNameTextView)
    val equipZavNumLabelTextView: TextView = view.findViewById(R.id.equipZavNumLabelTextView)
    val equipZavNumTextView: TextView = view.findViewById(R.id.equipZavNumTextView)
    val equipRFIDLabelTextView: TextView = view.findViewById(R.id.equipRFIDLabelTextView)
    val equipRFIDTextView: TextView = view.findViewById(R.id.equipRFIDTextView)
    val equipTagLabelTextView: TextView = view.findViewById(R.id.equipTagLabelTextView)
    val equipTagTextView: TextView = view.findViewById(R.id.equipTagTextView)
    val equipLocationLabelTextView: TextView = view.findViewById(R.id.equipLocationLabelTextView)
    val equipLocationTextView: TextView = view.findViewById(R.id.equipLocationTextView)
    val equipGRSILabelTextView: TextView = view.findViewById(R.id.equipGRSILabelTextView)
    val equipGRSITextView: TextView = view.findViewById(R.id.equipGRSITextView)
    val equipManufacturerLabelTextView: TextView = view.findViewById(R.id.equipManufacturerLabelTextView)
    val equipManufacturerTextView: TextView = view.findViewById(R.id.equipManufacturerTextView)
    val equipMeteringTypeLabelTextView: TextView = view.findViewById(R.id.equipMeteringTypeLabelTextView)
    val equipMeteringTypeTextView: TextView = view.findViewById(R.id.equipMeteringTypeTextView)
    val equipCalibrationLabelTextView: TextView = view.findViewById(R.id.equipCalibrationLabelTextView)
    val equipCalibrationTextView: TextView = view.findViewById(R.id.equipCalibrationTextView)
    val equipVerificationLabelTextView: TextView = view.findViewById(R.id.equipVerificationLabelTextView)
    val equipVerificationTextView: TextView = view.findViewById(R.id.equipVerificationTextView)
    val eventNameTextView: TextView = view.findViewById(R.id.eventNameTextView)
    val checkListSizeTextView: TextView = view.findViewById(R.id.checkListSizeTextView)
    val typeLabelTextView: TextView = view.findViewById(R.id.typeLabelTextView)
    val typeTextView: TextView = view.findViewById(R.id.typeTextView)
    val planDateLabelTextView: TextView = view.findViewById(R.id.planDateLabelTextView)
    val planDateTextView: TextView = view.findViewById(R.id.planDateTextView)
    val factDateLabelTextView: TextView = view.findViewById(R.id.factDateLabelTextView)
    val factDateTextView: TextView = view.findViewById(R.id.factDateTextView)

    val equipTagActualImageView: ImageView = view.findViewById(R.id.equipTagActualImageView)
    val equipFullInfoImageView: ImageView = view.findViewById(R.id.equipFullInfoImageView)
    val eventStatusImageView: ImageView = view.findViewById(R.id.eventStatusImageView)
    val equipLabelImageView: ImageView = view.findViewById(R.id.equipLabelImageView)

    var isFullInfo = false
}