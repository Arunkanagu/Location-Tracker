package dev.nura.locationtracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.nura.locationtracker.Utils.toFormattedDateTime
import dev.nura.locationtracker.databinding.LayoutLocationItemBinding
import dev.nura.locationtracker.realm.LocationInfo

class LocationAdapter(
    private var list: List<LocationInfo>,
    private val listener: OnClickListener
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    interface OnClickListener {
        fun onClick(item: LocationInfo)
    }

    init {
        setHasStableIds(true)
    }

    class LocationViewHolder(val binding: LayoutLocationItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationViewHolder {
        return LocationViewHolder(
            LayoutLocationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val loca = list[position]
        with(holder.binding) {

            date.text = loca.time.toFormattedDateTime()
            location.text = "Latitude: ${loca.latitude},\nLongitude: ${loca.longitude}"
            goMap.setOnClickListener {
                listener.onClick(loca)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}