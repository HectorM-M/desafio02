package com.example.myjourney

data class ItemCarrito(
    var id: String = "",
    var nombre: String = "",
    var pais: String = "",
    var precio: Double = 0.0,
    var descripcion: String = "",
    var imagenUrl: String = "",
    var cantidad: Int = 1
)