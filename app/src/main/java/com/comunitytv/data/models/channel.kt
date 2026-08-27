package com.comunitytv.data.models

data class Channel(
    val id: String = "",
    val name: String = "Sin nombre",
    val group: String = "General",
    val url: String = "",
    val logo: String = "",
    var isFavorite: Boolean = false
)
