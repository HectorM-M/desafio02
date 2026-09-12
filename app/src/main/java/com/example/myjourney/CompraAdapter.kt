package com.example.myjourney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class CompraAdapter(
    private val compras: List<Compra>
) : RecyclerView.Adapter<CompraAdapter.CompraViewHolder>() {

    class CompraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvPurchaseNumber: TextView =
            itemView.findViewById(R.id.tvPurchaseNumber)

        val tvPurchaseDate: TextView =
            itemView.findViewById(R.id.tvPurchaseDate)

        val tvPurchaseItems: TextView =
            itemView.findViewById(R.id.tvPurchaseItems)

        val tvPurchaseTotal: TextView =
            itemView.findViewById(R.id.tvPurchaseTotal)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CompraViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compra, parent, false)

        return CompraViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CompraViewHolder,
        position: Int
    ) {

        val compra = compras[position]

        holder.tvPurchaseNumber.text =
            "Compra #${position + 1}"

        val fecha = compra.fecha

        if (fecha != null) {

            val formato =
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                )

            holder.tvPurchaseDate.text =
                formato.format(fecha.toDate())

        } else {

            holder.tvPurchaseDate.text =
                "Fecha no disponible"
        }

        val nombres = StringBuilder()

        for (item in compra.items) {

            val nombre =
                item["nombre"]?.toString() ?: ""

            val pais =
                item["pais"]?.toString() ?: ""

            val cantidad =
                item["cantidad"]?.toString() ?: "1"

            nombres.append(
                "$nombre - $pais (x$cantidad)\n"
            )
        }

        holder.tvPurchaseItems.text =
            nombres.toString().trim()

        holder.tvPurchaseTotal.text =
            "Total: $${String.format("%.2f", compra.total)}"
    }

    override fun getItemCount(): Int {
        return compras.size
    }
}