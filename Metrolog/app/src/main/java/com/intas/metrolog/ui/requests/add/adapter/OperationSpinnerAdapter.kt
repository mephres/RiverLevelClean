package com.intas.metrolog.ui.requests.add.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.intas.metrolog.pojo.operation.EventOperationItem

class OperationSpinnerAdapter(
    val cont: Context,
    val textViewResourceId: Int,
    val values: List<EventOperationItem>
) : ArrayAdapter<EventOperationItem>(cont, textViewResourceId, values) {

    override fun getCount(): Int {
        return values.size
    }

    override fun getItem(position: Int): EventOperationItem {
        return values[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getView(position, convertView, parent) as TextView
        label.text = values[position].name
        return label
    }

    override fun getDropDownView(
        position: Int, convertView: View,
        parent: ViewGroup
    ): View {
        val label = super.getDropDownView(position, convertView, parent) as TextView
        label.text = values[position].name
        return label
    }
}
