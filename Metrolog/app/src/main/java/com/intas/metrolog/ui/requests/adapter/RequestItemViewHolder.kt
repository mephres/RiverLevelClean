package com.intas.metrolog.ui.requests.adapter

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intas.metrolog.R

class RequestItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val requestStatusTextView: TextView = view.findViewById(R.id.requestStatusTextView)
    val requestTitleTextView: TextView = view.findViewById(R.id.requestTitleTextView)
    val requestDateTextView: TextView = view.findViewById(R.id.requestDateTextView)
    val requestColorStatusTextView: TextView = view.findViewById(R.id.requestColorStatusTextView)
    val requestFromTextView: TextView = view.findViewById(R.id.requestFromTextView)
    val requestExecutorTextView: TextView = view.findViewById(R.id.requestExecutorTextView)
    val requestExecutorLabelTextView: TextView = view.findViewById(R.id.requestExecutorLabelTextView)
    val requestDisciplineOperationTextView: TextView = view.findViewById(R.id.requestDisciplineOperationTextView)
    val requestCommentTextView: TextView = view.findViewById(R.id.requestCommentTextView)
    val requestTypeImageView: ImageView = view.findViewById(R.id.requestTypeImageView)
    val requestIsNotSendImageView: ImageView = view.findViewById(R.id.requestIsNotSendImageView)

}
