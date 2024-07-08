package com.example.dripdropdigital

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Esta classe é responsável por associar os dados da lista de sistemas a um elemento do layout
class SystemListAdapter(private val system: List<SystemItem>, private val context: Context, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<SystemListAdapter.ViewHolder>() {

    // Interface para o evento de click
    interface OnItemClickListener {
        fun onItemClick(position: Int, system: SystemItem)
    }

    // Associa os dados da lista de sistemas a um elemento do layout
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val locate = system[position]
        holder.bindView(locate)
        // associa o item a um evento de click
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position, locate)
        }
    }

    // Cria um novo elemento do layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.system_item, parent, false)
        return ViewHolder(view)
    }

    // Retorna o número de elementos da lista de sistemas
    override fun getItemCount(): Int {
        return system.size
    }

    // Esta classe é responsável por associar os dados da lista de sistemas a um elemento do layout
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(systems: SystemItem) {
            val image: ImageView = itemView.findViewById(R.id.system_item_image)
            val title: TextView = itemView.findViewById(R.id.system_item_title)
            val location: TextView = itemView.findViewById(R.id.system_item_location)

            image.setImageResource(R.drawable.no_image)
            title.text = systems.title
            location.text = systems.location?.let { getStreetName(it) }
        }

        // Função para obter o nome da rua a partir das coordenadas
        private fun getStreetName(location: String): String {
            val coordinates = location.split(",")
            val latitude = coordinates[0].toDouble()
            val longitude = coordinates[1].toDouble()
            val geocoder = android.location.Geocoder(itemView.context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if( addresses != null) {
                if (addresses.isEmpty()) return "Rua desconhecida"
            } else return "Rua desconhecida"

            return addresses[0].getAddressLine(0)
        }

    }



    }