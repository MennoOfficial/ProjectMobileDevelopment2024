package com.example.lendlyapp.models

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Product(
    val id: String = "",
    val name: String = "",
    val details: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val userId: String = "",
    val tag: String = "",
    val location: GeoPoint? = null,
    val createdAt: Date = Date(),
    val status: String = "available",
    val rentalPeriods: List<RentalPeriod> = listOf()
) {
    constructor() : this("", "", "", 0.0, "", "", "", null, Date())
}