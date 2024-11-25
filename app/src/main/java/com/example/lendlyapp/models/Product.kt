package com.example.lendlyapp.models

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val details: String = "",
    val userId: String = "",
    val createdAt: Date = Date(),
    val location: GeoPoint? = null,
    val tag: String = ""
)