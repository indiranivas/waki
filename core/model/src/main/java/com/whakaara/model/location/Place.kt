package com.whakaara.model.location

data class Place(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distance: String? = null
)
