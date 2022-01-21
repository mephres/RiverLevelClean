package com.intas.metrolog.ui.requests.add.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.intas.metrolog.pojo.event.event_operation_type.EventOperationTypeItem

class OperationTypeSpinnerAdapter(
    val cont: Context,
    val textViewResourceId: Int,
    val values: List<EventOperationTypeItem>
) : ArrayAdapter<EventOperationTypeItem>(cont, textViewResourceId, values) {

    override fun getCount(): Int {
        return values.size
    }

    override fun getItem(position: Int): EventOperationTypeItem {
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
