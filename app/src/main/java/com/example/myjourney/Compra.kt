package com.example.myjourney

data class Compra(
    var id: String = "",
    var usuarioUid: String = "",
    var total: Double = 0.0,
    var fecha: com.google.firebase.Timestamp? = null,
    var items: List<HashMap<String, Any>> = emptyList()
)