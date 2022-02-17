package com.intas.metrolog.ui.operation.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.intas.metrolog.R
import com.intas.metrolog.pojo.event.event_photo.EventPhotoItem
import com.intas.metrolog.ui.operation.adapter.callback.EventPhotoItemDiffCallback
import com.intas.metrolog.util.FileUtil
import com.intas.metrolog.util.FileUtil.Companion.getStringSizeLengthFile
import com.intas.metrolog.util.Util
import java.io.File

class EventPhotoListAdapter: ListAdapter<EventPhotoItem, EventPhotoItemViewHolder>(
    EventPhotoItemDiffCallback()
) {

    var onEventPhotoItemClickListener: ((EventPhotoItem) -> Unit)? = null
    lateinit var context: Context

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventPhotoItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_photo_item, parent, false)
        context = parent.context
        return EventPhotoItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventPhotoItemViewHolder, position: Int) {

        val eventPhotoItem = getItem(position)

        eventPhotoItem.photoUri?.let { uri->

            FileUtil.setContext(context)

            val file = File(FileUtil.getPath(Uri.parse(uri)))
            val fileType = file.extension.lowercase()
            val fileName = file.nameWithoutExtension
            val fileSize = file.getStringSizeLengthFile()

            when(fileType) {
                "jpg", "png" -> {
                    Glide.with(context).load(file).into(holder.eventPhotoItemImageImageView)
                }
                "pdf", "doc", "docx", "rtf", "txt", "xls", "xlsx" -> {
                    val resourceName = "filetypes_${fileType}_256px"
                    val resourceId = Util.getResId(resourceName, R.drawable::class.java)
                    Glide.with(context).load(resourceId).into(holder.eventPhotoItemImageImageView)
                }
                else -> {
                    Glide.with(context).load(R.drawable.ic_baseline_no_photography_black_96dp).into(holder.eventPhotoItemImageImageView)
                }
            }
            holder.eventPhotoItemNameTextView.text = fileName
            holder.eventPhotoItemInfoTextView.text = "$fileSize - ${fileType.uppercase()}"
        }

        holder.itemView.setOnClickListener {
            onEventPhotoItemClickListener?.invoke(eventPhotoItem)
        }
    }

    override fun onViewRecycled(holder: EventPhotoItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}