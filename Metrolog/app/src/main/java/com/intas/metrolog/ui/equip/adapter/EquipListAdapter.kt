package com.intas.metrolog.ui.equip.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.intas.metrolog.R
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.equip.callback.EquipItemDiffCallback

class EquipListAdapter : ListAdapter<EquipItem, EquipItemViewHolder>(EquipItemDiffCallback()) {

    lateinit var context: Context
    var onAddRFIDButtonClickListener: ((EquipItem) -> Unit)? = null
    var onCreateDocumentButtonListener: ((EquipItem) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.equip_item, parent, false)
        context = parent.context
        return EquipItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipItemViewHolder, position: Int) {

        val equipItem = getItem(position)

        holder.equipNameTextView.text = equipItem.equipName
        holder.equipZavNumTextView.text = equipItem.equipZavNum

        if (!equipItem.equipRFID.isNullOrEmpty()) {
            holder.equipRFIDTextView.text = equipItem.equipRFID
            holder.equipRFIDTextView.setBackgroundResource(R.drawable.rounded_corner_area_green)
            holder.equipRFIDTextView.setTextColor(Color.WHITE)
        } else {
            holder.equipRFIDTextView.text = context.getString(R.string.no_data)
            holder.equipRFIDTextView.setBackgroundResource(R.drawable.rounded_corner_area_red)
            holder.equipRFIDTextView.setTextColor(Color.WHITE)
        }

        if (!equipItem.equipTag.isNullOrEmpty()) {
            holder.equipTagTextView.text = equipItem.equipTag
            holder.equipTagTextView.setBackgroundResource(R.drawable.rounded_corner_area_white)
            holder.equipTagTextView.background
                .setTint(ContextCompat.getColor(context, R.color.md_white_1000))
            holder.equipTagTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorSecondaryTextMaterialLight
                )
            )
        } else {
            holder.equipTagTextView.text = context.getString(R.string.no_data)
            holder.equipTagTextView.setBackgroundResource(R.drawable.rounded_corner_area_red)
            holder.equipTagTextView.setTextColor(Color.WHITE)
        }

        when (equipItem.equipTagActual) {
            0 -> {
                holder.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_baseline_cancel_black_24dp
                    )
                )
                holder.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.md_red_700
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
            1 -> {
                holder.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_baseline_check_circle_black_24dp
                    )
                )
                holder.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
            else -> {
                holder.equipTagActualImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_warning_red_24dp
                    )
                )
                holder.equipTagActualImageView.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.md_deep_orange_A200
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
        }

        if (!equipItem.mestUstan.isNullOrEmpty()) {
            holder.equipLocationTextView.text = equipItem.mestUstan
            holder.equipLocationTextView.setBackgroundResource(R.drawable.rounded_corner_area_white)
            holder.equipLocationTextView.background
                .setTint(ContextCompat.getColor(context, R.color.md_white_1000))
            holder.equipLocationTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorSecondaryTextMaterialLight
                )
            )
        } else {
            holder.equipLocationTextView.text = context.getString(R.string.no_data)
            holder.equipLocationTextView.setBackgroundResource(R.drawable.rounded_corner_area_red)
            holder.equipLocationTextView.setTextColor(Color.WHITE)
        }

        if (!equipItem.equipGRSI.isNullOrEmpty()) {
            holder.equipGRSILabelTextView.visibility = View.VISIBLE
            holder.equipGRSITextView.visibility = View.VISIBLE
            holder.equipGRSITextView.text = equipItem.equipGRSI
        } else {
            holder.equipGRSILabelTextView.visibility = View.GONE
            holder.equipGRSITextView.visibility = View.GONE
        }

        if (!equipItem.equipZavodIzg.isNullOrEmpty()) {
            holder.equipManufacturerLabelTextView.visibility = View.VISIBLE
            holder.equipManufacturerTextView.visibility = View.VISIBLE
            holder.equipManufacturerTextView.text = equipItem.equipZavodIzg
        } else {
            holder.equipManufacturerLabelTextView.visibility = View.GONE
            holder.equipManufacturerTextView.visibility = View.GONE
        }

        if (!equipItem.equipVidIzm.isNullOrEmpty()) {
            holder.equipMeteringTypeLabelTextView.visibility = View.VISIBLE
            holder.equipMeteringTypeTextView.visibility = View.VISIBLE
            holder.equipMeteringTypeTextView.text = equipItem.equipVidIzm
        } else {
            holder.equipMeteringTypeLabelTextView.visibility = View.GONE
            holder.equipMeteringTypeTextView.visibility = View.GONE
        }

        if (!equipItem.lastCalibr.isNullOrEmpty()) {
            holder.equipCalibrationLabelTextView.visibility = View.VISIBLE
            holder.equipCalibrationTextView.visibility = View.VISIBLE
            holder.equipCalibrationTextView.text = equipItem.lastCalibr
        } else {
            holder.equipCalibrationLabelTextView.visibility = View.GONE
            holder.equipCalibrationTextView.visibility = View.GONE
        }

        if (!equipItem.lastVerif.isNullOrEmpty()) {
            holder.equipVerificationLabelTextView.visibility = View.VISIBLE
            holder.equipVerificationTextView.visibility = View.VISIBLE
            holder.equipVerificationTextView.text = equipItem.lastVerif
        } else {
            holder.equipVerificationLabelTextView.visibility = View.GONE
            holder.equipVerificationTextView.visibility = View.GONE
        }

        if (equipItem.isSendRFID == 0 || equipItem.isSendGeo == 0) {
            holder.equipIsNotSendImageView.visibility = View.VISIBLE
        } else {
            holder.equipIsNotSendImageView.visibility = View.GONE
        }

        holder.addRFIDButton.setOnClickListener {
            onAddRFIDButtonClickListener?.invoke(equipItem)
        }

        holder.createDocumentButton.setOnClickListener {
            onCreateDocumentButtonListener?.invoke(equipItem)
        }
    }

    override fun onViewRecycled(holder: EquipItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }


}