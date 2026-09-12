package com.example.myjourney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class ItemCarritoAdapter(
    private val items: List<ItemCarrito>
) : RecyclerView.Adapter<ItemCarritoAdapter.ItemCarritoViewHolder>() {

    class ItemCarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivCartImage: ImageView =
            itemView.findViewById(R.id.ivCartImage)

        val tvCartName: TextView =
            itemView.findViewById(R.id.tvCartName)

        val tvCartCountry: TextView =
            itemView.findViewById(R.id.tvCartCountry)

        val tvCartPrice: TextView =
            itemView.findViewById(R.id.tvCartPrice)

        val tvCartQuantity: TextView =
            itemView.findViewById(R.id.tvCartQuantity)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemCarritoViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)

        return ItemCarritoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ItemCarritoViewHolder,
        position: Int
    ) {

        val item = items[position]

        holder.tvCartName.text = item.nombre
        holder.tvCartCountry.text = item.pais
        holder.tvCartPrice.text = "$${item.precio}"
        holder.tvCartQuantity.text = "Cantidad: ${item.cantidad}"

        Glide.with(holder.itemView.context)
            .load(File(item.imagenUrl))
            .into(holder.ivCartImage)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}