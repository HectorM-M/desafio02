package com.example.myjourney

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistorialComprasActivity : AppCompatActivity() {

    private lateinit var recyclerPurchaseHistory: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val compras = mutableListOf<Compra>()

    private lateinit var adapter: CompraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_historial_compras)

        recyclerPurchaseHistory =
            findViewById(R.id.recyclerPurchaseHistory)

        recyclerPurchaseHistory.layoutManager =
            LinearLayoutManager(this)

        adapter = CompraAdapter(compras)

        recyclerPurchaseHistory.adapter = adapter

        cargarHistorial()
    }

    private fun cargarHistorial() {

        val usuario = auth.currentUser

        if (usuario == null) {
            return
        }

        db.collection("compras")
            .whereEqualTo("usuarioUid", usuario.uid)
            .get()
            .addOnSuccessListener { result ->

                compras.clear()

                for (document in result) {

                    val compra =
                        document.toObject(Compra::class.java)

                    compra.id = document.id

                    compras.add(compra)
                }

                // Mostrar las compras más recientes primero
                compras.reverse()

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "No se pudo cargar el historial de compras",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}