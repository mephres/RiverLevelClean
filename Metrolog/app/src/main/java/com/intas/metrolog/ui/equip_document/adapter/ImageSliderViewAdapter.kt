package com.intas.metrolog.ui.equip_document.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.intas.metrolog.R
import com.smarteist.autoimageslider.SliderViewAdapter

class ImageSliderViewAdapter(
    private val uriList: List<Uri>
) : SliderViewAdapter<ImageSliderViewHolder>() {

    lateinit var context: Context
    var onCropImageListener: ((Uri) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup): ImageSliderViewHolder {
        context = parent.context
        val inflate = LayoutInflater.from(context).inflate(R.layout.image_slider_item, null)
        return ImageSliderViewHolder(inflate)
    }

    override fun onBindViewHolder(
        holder: ImageSliderViewHolder,
        position: Int
    ) {

        holder.imageSliderAutoTextView.text = ""

        Glide.with(context)
            .load(uriList[position])
            .into(holder.imageSliderImageView)

        holder.imageSliderImageView.setOnClickListener {
            onCropImageListener?.invoke(uriList[position])
        }
    }

    override fun getCount(): Int {
        return uriList.size
    }
}
