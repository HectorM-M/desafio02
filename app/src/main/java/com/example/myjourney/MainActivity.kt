package com.example.myjourney

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerDestinations: RecyclerView

    private val db = FirebaseFirestore.getInstance()

    private val auth = FirebaseAuth.getInstance()

    private val adminUid =
        "0RSPHmKtkTOWVadX1A22qfzmYgD3"

    private val destinos = mutableListOf<Destino>()

    private lateinit var adapter: DestinoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        recyclerDestinations =
            findViewById(R.id.recyclerDestinations)

        recyclerDestinations.layoutManager =
            LinearLayoutManager(this)

        // Verificar si el usuario actual es administrador
        val esAdministrador =
            auth.currentUser?.uid == adminUid

        // Configurar RecyclerView
        adapter = DestinoAdapter(
            destinos,
            esAdministrador,

            onEditClick = { destino ->

                if (esAdministrador) {
                    editarDestino(destino)
                }

            },

            onDeleteClick = { destino ->

                if (esAdministrador) {
                    confirmarEliminacion(destino)
                }

            },

            onAddToCartClick = { destino ->

                val usuario = auth.currentUser

                if (usuario != null) {

                    val itemCarrito = hashMapOf(
                        "nombre" to destino.nombre,
                        "pais" to destino.pais,
                        "precio" to destino.precio,
                        "descripcion" to destino.descripcion,
                        "imagenUrl" to destino.imagenUrl,
                        "cantidad" to 1
                    )

                    db.collection("carritos")
                        .document(usuario.uid)
                        .collection("items")
                        .document(destino.id)
                        .set(itemCarrito)
                        .addOnSuccessListener {

                            Toast.makeText(
                                this,
                                getString(R.string.added_to_cart),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {

                            Toast.makeText(
                                this,
                                getString(R.string.destination_save_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        )

        recyclerDestinations.adapter = adapter

        // Botón agregar destino
        val btnAddDestination =
            findViewById<Button>(R.id.btnAddDestination)

        if (!esAdministrador) {
            btnAddDestination.visibility = Button.GONE
        }

        btnAddDestination.setOnClickListener {

            if (esAdministrador) {

                val intent =
                    Intent(this, AgregarDestinoActivity::class.java)

                startActivity(intent)
            }
        }

        // Botón ver carrito
        val btnViewCart =
            findViewById<Button>(R.id.btnViewCart)

        if (esAdministrador) {
            btnViewCart.visibility = Button.GONE
        }

        btnViewCart.setOnClickListener {

            if (!esAdministrador) {

                val intent =
                    Intent(this, CarritoActivity::class.java)

                startActivity(intent)
            }
        }
        // Botón historial de compras
        val btnPurchaseHistory =
            findViewById<Button>(R.id.btnPurchaseHistory)

        if (esAdministrador) {
            btnPurchaseHistory.visibility = Button.GONE
        }

        btnPurchaseHistory.setOnClickListener {

            if (!esAdministrador) {

                val intent =
                    Intent(
                        this,
                        HistorialComprasActivity::class.java
                    )

                startActivity(intent)
            }
        }

        // Botón cerrar sesión
        val btnLogout =
            findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle(
                    getString(R.string.logout)
                )
                .setMessage(
                    getString(R.string.logout_confirmation)
                )
                .setNegativeButton(
                    getString(R.string.cancel),
                    null
                )
                .setPositiveButton(
                    getString(R.string.confirm)
                ) { _, _ ->

                    // Cerrar sesión en Firebase
                    auth.signOut()

                    // Regresar al Login y limpiar el historial
                    val intent =
                        Intent(this, LoginActivity::class.java)

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)

                    finish()
                }
                .show()
        }

        // Cargar destinos
        cargarDestinos()
    }

    override fun onResume() {
        super.onResume()

        cargarDestinos()
    }

    private fun cargarDestinos() {

        db.collection("destinos")
            .get()
            .addOnSuccessListener { result ->

                destinos.clear()

                for (document in result) {

                    val destino =
                        document.toObject(Destino::class.java)

                    destino.id = document.id

                    destinos.add(destino)
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun editarDestino(destino: Destino) {

        val intent =
            Intent(this, AgregarDestinoActivity::class.java)

        intent.putExtra(
            "destino_id",
            destino.id
        )

        intent.putExtra(
            "destino_nombre",
            destino.nombre
        )

        intent.putExtra(
            "destino_pais",
            destino.pais
        )

        intent.putExtra(
            "destino_precio",
            destino.precio
        )

        intent.putExtra(
            "destino_descripcion",
            destino.descripcion
        )

        intent.putExtra(
            "destino_imagen",
            destino.imagenUrl
        )

        startActivity(intent)
    }

    private fun confirmarEliminacion(destino: Destino) {

        AlertDialog.Builder(this)
            .setTitle(
                getString(R.string.delete_title)
            )
            .setMessage(
                getString(R.string.delete_confirmation)
            )
            .setNegativeButton(
                getString(R.string.cancel),
                null
            )
            .setPositiveButton(
                getString(R.string.confirm)
            ) { _, _ ->

                eliminarDestino(destino)
            }
            .show()
    }

    private fun eliminarDestino(destino: Destino) {

        db.collection("destinos")
            .document(destino.id)
            .delete()
            .addOnSuccessListener {

                val posicion =
                    destinos.indexOfFirst {
                        it.id == destino.id
                    }

                if (posicion != -1) {

                    destinos.removeAt(posicion)

                    adapter.notifyItemRemoved(posicion)
                }

                Toast.makeText(
                    this,
                    getString(R.string.destination_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    getString(R.string.destination_delete_error),
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
