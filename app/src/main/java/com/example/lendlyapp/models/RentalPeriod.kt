package com.example.lendlyapp.models

data class RentalPeriod(
    val startDate: Long = 0,
    val endDate: Long = 0,
    val renterId: String = "",
    val totalPrice: Double = 0.0
) {
    constructor() : this(0, 0, "", 0.0)
} 