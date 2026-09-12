package com.example.myjourney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class DestinoAdapter(
    private val destinos: List<Destino>,
    private val esAdministrador: Boolean,
    private val onEditClick: (Destino) -> Unit,
    private val onDeleteClick: (Destino) -> Unit,
    private val onAddToCartClick: (Destino) -> Unit
) : RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

    class DestinoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivDestination: ImageView =
            itemView.findViewById(R.id.ivDestination)

        val tvDestinationName: TextView =
            itemView.findViewById(R.id.tvDestinationName)

        val tvDestinationCountry: TextView =
            itemView.findViewById(R.id.tvDestinationCountry)

        val tvDestinationPrice: TextView =
            itemView.findViewById(R.id.tvDestinationPrice)

        val tvDestinationDescription: TextView =
            itemView.findViewById(R.id.tvDestinationDescription)

        val btnAddToCart: Button =
            itemView.findViewById(R.id.btnAddToCart)

        val btnEditDestination: Button =
            itemView.findViewById(R.id.btnEditDestination)

        val btnDeleteDestination: Button =
            itemView.findViewById(R.id.btnDeleteDestination)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DestinoViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destino, parent, false)

        return DestinoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DestinoViewHolder,
        position: Int
    ) {

        val destino = destinos[position]

        holder.tvDestinationName.text = destino.nombre
        holder.tvDestinationCountry.text = destino.pais
        holder.tvDestinationPrice.text = "$${destino.precio}"
        holder.tvDestinationDescription.text = destino.descripcion

        Glide.with(holder.itemView.context)
            .load(File(destino.imagenUrl))
            .into(holder.ivDestination)

        if (esAdministrador) {

            // El administrador NO compra
            holder.btnAddToCart.visibility = View.GONE

            // El administrador puede editar y eliminar
            holder.btnEditDestination.visibility = View.VISIBLE
            holder.btnDeleteDestination.visibility = View.VISIBLE

            holder.btnEditDestination.setOnClickListener {
                onEditClick(destino)
            }

            holder.btnDeleteDestination.setOnClickListener {
                onDeleteClick(destino)
            }

        } else {

            // Los clientes pueden agregar destinos al carrito
            holder.btnAddToCart.visibility = View.VISIBLE

            // Los clientes NO pueden editar ni eliminar
            holder.btnEditDestination.visibility = View.GONE
            holder.btnDeleteDestination.visibility = View.GONE

            holder.btnAddToCart.setOnClickListener {
                onAddToCartClick(destino)
            }
        }
    }

    override fun getItemCount(): Int {
        return destinos.size
    }
}
