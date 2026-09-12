package com.example.myjourney

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class AgregarDestinoActivity : AppCompatActivity() {

    private lateinit var ivSelectedImage: ImageView

    private var imageUri: Uri? = null
    private var imageLocalPath: String? = null

    private val db = FirebaseFirestore.getInstance()

    // Saber si estamos agregando o editando
    private var destinoId: String? = null
    private var imagenAnterior: String? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

            if (uri != null) {
                imageUri = uri
                imageLocalPath = guardarImagenLocal(uri)

                ivSelectedImage.setImageURI(uri)
                ivSelectedImage.visibility = ImageView.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_destino)

        val etDestinationName =
            findViewById<EditText>(R.id.etDestinationName)

        val spCountry =
            findViewById<Spinner>(R.id.spCountry)

        val etPrice =
            findViewById<EditText>(R.id.etPrice)

        val etDescription =
            findViewById<EditText>(R.id.etDescription)

        ivSelectedImage =
            findViewById(R.id.ivSelectedImage)

        val btnSelectImage =
            findViewById<Button>(R.id.btnSelectImage)

        val btnSaveDestination =
            findViewById<Button>(R.id.btnSaveDestination)

        // Configurar países
        val countries = arrayOf(
            getString(R.string.country_select),
            getString(R.string.country_el_salvador),
            getString(R.string.country_guatemala),
            getString(R.string.country_mexico),
            getString(R.string.country_usa),
            getString(R.string.country_canada),
            getString(R.string.country_spain),
            getString(R.string.country_france),
            "Japon",
            getString(R.string.country_italy),
            "Egipto"
        )

        val countryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            countries
        )

        countryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spCountry.adapter = countryAdapter

        // Comprobar si estamos editando
        destinoId = intent.getStringExtra("destino_id")
        imagenAnterior = intent.getStringExtra("destino_imagen")

        if (destinoId != null) {

            etDestinationName.setText(
                intent.getStringExtra("destino_nombre")
            )

            etPrice.setText(
                intent.getDoubleExtra("destino_precio", 0.0).toString()
            )

            etDescription.setText(
                intent.getStringExtra("destino_descripcion")
            )

            val pais = intent.getStringExtra("destino_pais")

            val posicionPais =
                countries.indexOf(pais)

            if (posicionPais >= 0) {
                spCountry.setSelection(posicionPais)
            }

            if (!imagenAnterior.isNullOrEmpty()) {

                val archivo = File(imagenAnterior!!)

                if (archivo.exists()) {
                    ivSelectedImage.setImageURI(
                        Uri.fromFile(archivo)
                    )

                    ivSelectedImage.visibility =
                        ImageView.VISIBLE

                    imageLocalPath = imagenAnterior
                }
            }

            btnSaveDestination.setText(
                getString(R.string.update_destination)
            )
        }

        // Seleccionar imagen
        btnSelectImage.setOnClickListener {
            imagePicker.launch(arrayOf("image/*"))
        }

        // Guardar o actualizar
        btnSaveDestination.setOnClickListener {

            val name =
                etDestinationName.text.toString().trim()

            val country =
                spCountry.selectedItem.toString()

            val priceText =
                etPrice.text.toString().trim()

            val description =
                etDescription.text.toString().trim()

            // Validar nombre
            if (name.isEmpty()) {
                etDestinationName.error =
                    getString(R.string.required_field)

                etDestinationName.requestFocus()
                return@setOnClickListener
            }

            // Validar país
            if (spCountry.selectedItemPosition == 0) {

                Toast.makeText(
                    this,
                    getString(R.string.country_required),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Validar precio
            if (priceText.isEmpty()) {

                etPrice.error =
                    getString(R.string.required_field)

                etPrice.requestFocus()
                return@setOnClickListener
            }

            val price =
                priceText.toDoubleOrNull()

            if (price == null || price <= 0) {

                etPrice.error =
                    getString(R.string.invalid_price)

                etPrice.requestFocus()
                return@setOnClickListener
            }

            // Validar descripción
            if (description.isEmpty()) {

                etDescription.error =
                    getString(R.string.required_field)

                etDescription.requestFocus()
                return@setOnClickListener
            }

            if (description.length < 20) {

                etDescription.error =
                    getString(R.string.invalid_description)

                etDescription.requestFocus()
                return@setOnClickListener
            }

            // Validar imagen
            if (imageLocalPath == null) {

                Toast.makeText(
                    this,
                    getString(R.string.image_required),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Crear datos del destino
            val destino = hashMapOf(
                "nombre" to name,
                "pais" to country,
                "precio" to price,
                "descripcion" to description,
                "imagenUrl" to imageLocalPath
            )

            // UPDATE
            if (destinoId != null) {

                db.collection("destinos")
                    .document(destinoId!!)
                    .set(destino)
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            getString(R.string.destination_updated),
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            this,
                            getString(R.string.destination_update_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }

            } else {

                // CREATE
                db.collection("destinos")
                    .add(destino)
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            getString(R.string.destination_saved),
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
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
    }

    private fun guardarImagenLocal(uri: Uri): String {

        val nombreArchivo =
            "destino_${System.currentTimeMillis()}.jpg"

        val archivo =
            File(filesDir, nombreArchivo)

        contentResolver.openInputStream(uri).use { inputStream ->

            archivo.outputStream().use { outputStream ->

                inputStream?.copyTo(outputStream)
            }
        }

        return archivo.absolutePath
    }
}