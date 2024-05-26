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
class SystemListAdapter(private val system: List<SystemID>, private val context: Context, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<SystemListAdapter.ViewHolder>() {

    // Interface para o evento de click
    interface OnItemClickListener {
        fun onItemClick(position: Int, location: SystemID)
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
        fun bindView(systems: SystemID) {
            val image: ImageView = itemView.findViewById(R.id.system_item_image)
            val title: TextView = itemView.findViewById(R.id.system_item_title)
            val location: TextView = itemView.findViewById(R.id.system_item_location)

            // Valida a imagem
            if (systems.image == "noimage"){
                image.setImageResource(R.drawable.no_image)
            } else {
                val decodedImage = decodeBase64(systems.image)
                decodedImage?.let {
                    image.setImageBitmap(it)
                }
            }

            title.text = systems.title
            location.text = systems.location
        }

        // função para converter uma string Base64 para bitmap
        private fun decodeBase64(encodedString: String?): Bitmap? {
            if (encodedString.isNullOrEmpty()) return null

            return try {
                Log.d("Base64", "Encoded String: $encodedString")
                val decodedBytes = android.util.Base64.decode(encodedString, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: IllegalArgumentException) {
                // Handle the case where decoding fails
                e.printStackTrace()
                null
            }
        }
    }
    }