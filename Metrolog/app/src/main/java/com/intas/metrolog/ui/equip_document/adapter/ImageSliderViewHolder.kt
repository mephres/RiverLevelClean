package com.intas.metrolog.ui.equip_document.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.intas.metrolog.R
import com.smarteist.autoimageslider.SliderViewAdapter

class ImageSliderViewHolder(val view: View) : SliderViewAdapter.ViewHolder(view) {
    val imageSliderImageView: ImageView = view.findViewById(R.id.imageSliderImageView)
    val imageSliderAutoTextView: TextView = view.findViewById(R.id.imageSliderAutoTextView)
    val imageSliderDeleteImageView: ImageView = view.findViewById(R.id.imageSliderDeleteImageView)
}