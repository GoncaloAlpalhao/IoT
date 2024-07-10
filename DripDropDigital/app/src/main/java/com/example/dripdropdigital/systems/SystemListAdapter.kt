package com.example.dripdropdigital.systems

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dripdropdigital.R

/**
 * Adapter for displaying a list of systems in a RecyclerView
 * @param system The list of systems
 * @param context The context of the application
 * @param itemClickListener The click event
 */
class SystemListAdapter(
    private val system: List<SystemItem>,
    private val context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<SystemListAdapter.ViewHolder>() {

    /**
     * Interface for the click event
     */
    interface OnItemClickListener {
        fun onItemClick(position: Int, system: SystemItem)
    }

    /**
     * This function is responsible for associating the data of the list of systems with a layout element
     * @param holder The ViewHolder
     * @param position The position of the item in the list
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val locate = system[position]
        holder.bindView(locate)
        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position, locate)
        }
    }

    /**
     * This function is responsible for creating a new ViewHolder
     * @param parent The parent ViewGroup
     * @param viewType The type of view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.system_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * This function is responsible for returning the number of items in the list
     * @return The number of items in the list
     */
    override fun getItemCount(): Int {
        return system.size
    }

    /**
     * This class is responsible for holding the elements of the layout
     * @param itemView The view
     * @constructor Creates an instance of ViewHolder
     * @return The ViewHolder
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * This function is responsible for binding the data of the system to the layout elements
         * @param systems The system information
         */
        fun bindView(systems: SystemItem) {
            val image: ImageView = itemView.findViewById(R.id.system_item_image)
            val title: TextView = itemView.findViewById(R.id.system_item_title)
            val location: TextView = itemView.findViewById(R.id.system_item_location)

            image.setImageResource(R.drawable.no_image)
            title.text = systems.title
            location.text = systems.location?.let { getStreetName(it) }
        }

        /**
         * This function is responsible for getting the street name of the location
         * @param location The location
         * @return The street name
         */
        private fun getStreetName(location: String): String {
            val coordinates = location.split(",")
            val latitude = coordinates[0].toDouble()
            val longitude = coordinates[1].toDouble()
            val geocoder = android.location.Geocoder(itemView.context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isEmpty()) return "Rua desconhecida"
            } else return "Rua desconhecida"

            return addresses[0].getAddressLine(0)
        }

    }


}