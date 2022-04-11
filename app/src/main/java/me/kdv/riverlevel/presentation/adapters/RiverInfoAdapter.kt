package me.kdv.riverlevel.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import me.kdv.riverlevel.R
import me.kdv.riverlevel.databinding.ItemRiverInfoBinding
import me.kdv.riverlevel.domain.RiverInfo
import kotlin.math.absoluteValue

class RiverInfoAdapter(private var context: Context) : ListAdapter<RiverInfo, RiverInfoViewHolder>(
    RiverInfoDiffCallback()
) {

    var onRiverItemClickListener: ((RiverInfo) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiverInfoViewHolder {
        val binding = ItemRiverInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RiverInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RiverInfoViewHolder, position: Int) {
        val river = getItem(position)

        with(holder.binding) {
            with(river) {

                tvName.text = river.name
                Glide.with(context).load(R.drawable.ic_baseline_location_on_24).into(ivLocation)
                tvLocation.text = river.location
                tvFloodplane.text = river.floodplain
                tvWaterLevel.text = river.waterLevel

                tvLevelChange.text = river.levelChange.toInt().absoluteValue.toString()
                val arrowIcon = getLevelChangeIcon(river.levelChange.toInt())
                Glide.with(context).load(arrowIcon).into(ivLevelChange)

                val tempPattern = context.resources.getString(R.string.river_info_water_temperature_pattern)
                tvWaterTemperature.text = "Н/Д"
                river.waterTemperature?.let {
                    tvWaterTemperature.text = String.format(tempPattern, it)
                }
                tvUpdateDateTime.text = river.dateTime
            }

            root.setOnClickListener {
                onRiverItemClickListener?.invoke(river)
            }
        }
    }

    private fun getLevelChangeIcon(levelChange: Int) = if (levelChange >= 0) {
        R.drawable.ic_baseline_arrow_drop_up_24
    } else {
        R.drawable.ic_baseline_arrow_drop_down_24
    }

    companion object {
        const val MAX_POOL_SIZE = 15
    }
}