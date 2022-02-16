package com.intas.metrolog.ui.operation.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.intas.metrolog.R

class EventPhotoItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val eventPhotoItemNameTextView = view.findViewById<TextView>(R.id.eventPhotoItemNameTextView)
    val eventPhotoItemInfoTextView = view.findViewById<TextView>(R.id.eventPhotoItemInfoTextView)
    val eventPhotoItemImageImageView = view.findViewById<ShapeableImageView>(R.id.eventPhotoItemImageImageView)
}