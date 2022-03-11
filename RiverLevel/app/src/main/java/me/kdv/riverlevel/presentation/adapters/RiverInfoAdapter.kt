package me.kdv.riverlevel.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import me.kdv.riverlevel.R
import me.kdv.riverlevel.databinding.ItemRiverInfoBinding
import me.kdv.riverlevel.domain.RiverInfo

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
                /*val symbolsTemplate = context.resources.getString(R.string.symbols_template)
                val updateTimeTemplate = context.resources.getString(R.string.update_time_template)
                Picasso.get().load(coin.imageUrl).into(logoCoinImageView)*/
                tvName.text = river.name
                    //String.format(symbolsTemplate, fromSymbol, toSymbol)
                /*priceTextView.text = price.toString()
                updateTimeTextView.text = String.format(updateTimeTemplate, coin.lastUpdate)*/
            }

            root.setOnClickListener {
                onRiverItemClickListener?.invoke(river)
            }
        }
    }

    companion object {
        const val MAX_POOL_SIZE = 15
    }
}