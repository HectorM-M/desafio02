package com.example.myjourney

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CarritoActivity : AppCompatActivity() {

    private lateinit var recyclerCart: RecyclerView
    private lateinit var tvCartTotal: TextView
    private lateinit var btnBuy: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val items = mutableListOf<ItemCarrito>()

    private lateinit var adapter: ItemCarritoAdapter

    private var totalCarrito = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_carrito)

        recyclerCart = findViewById(R.id.recyclerCart)
        tvCartTotal = findViewById(R.id.tvCartTotal)
        btnBuy = findViewById(R.id.btnBuy)

        recyclerCart.layoutManager =
            LinearLayoutManager(this)

        adapter = ItemCarritoAdapter(items)

        recyclerCart.adapter = adapter

        btnBuy.setOnClickListener {
            confirmarCompra()
        }

        cargarCarrito()
    }

    private fun cargarCarrito() {

        val usuario = auth.currentUser

        if (usuario == null) {
            return
        }

        db.collection("carritos")
            .document(usuario.uid)
            .collection("items")
            .get()
            .addOnSuccessListener { result ->

                items.clear()

                totalCarrito = 0.0

                for (document in result) {

                    val item =
                        document.toObject(ItemCarrito::class.java)

                    item.id = document.id

                    items.add(item)

                    totalCarrito +=
                        item.precio * item.cantidad
                }

                adapter.notifyDataSetChanged()

                tvCartTotal.text =
                    getString(
                        R.string.purchase_total,
                        totalCarrito
                    )

                btnBuy.isEnabled = items.isNotEmpty()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    getString(R.string.purchase_error),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun confirmarCompra() {

        if (items.isEmpty()) {

            Toast.makeText(
                this,
                getString(R.string.empty_cart),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        AlertDialog.Builder(this)
            .setTitle(
                getString(R.string.buy_confirmation_title)
            )
            .setMessage(
                getString(
                    R.string.purchase_total,
                    totalCarrito
                ) + "\n\n" +
                        getString(
                            R.string.buy_confirmation_message
                        )
            )
            .setNegativeButton(
                getString(R.string.cancel),
                null
            )
            .setPositiveButton(
                getString(R.string.confirm)
            ) { _, _ ->

                realizarCompra()
            }
            .show()
    }

    private fun realizarCompra() {

        val usuario = auth.currentUser

        if (usuario == null) {
            return
        }

        btnBuy.isEnabled = false

        // Convertir los productos del carrito
        // en una lista que Firestore pueda guardar
        val productos = items.map { item ->

            hashMapOf(
                "id" to item.id,
                "nombre" to item.nombre,
                "pais" to item.pais,
                "precio" to item.precio,
                "descripcion" to item.descripcion,
                "imagenUrl" to item.imagenUrl,
                "cantidad" to item.cantidad
            )
        }

        // Crear documento de compra
        val compra = hashMapOf(
            "usuarioUid" to usuario.uid,
            "total" to totalCarrito,
            "fecha" to FieldValue.serverTimestamp(),
            "items" to productos
        )

        // Referencia a una nueva compra
        val compraRef =
            db.collection("compras").document()

        // Batch para guardar compra
        // y eliminar carrito al mismo tiempo
        val batch = db.batch()

        batch.set(compraRef, compra)

        for (item in items) {

            val itemRef =
                db.collection("carritos")
                    .document(usuario.uid)
                    .collection("items")
                    .document(item.id)

            batch.delete(itemRef)
        }

        batch.commit()
            .addOnSuccessListener {

                items.clear()

                totalCarrito = 0.0

                adapter.notifyDataSetChanged()

                tvCartTotal.text =
                    getString(
                        R.string.purchase_total,
                        totalCarrito
                    )

                btnBuy.isEnabled = false

                Toast.makeText(
                    this,
                    getString(R.string.purchase_success),
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener {

                btnBuy.isEnabled = true

                Toast.makeText(
                    this,
                    getString(R.string.purchase_error),
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}