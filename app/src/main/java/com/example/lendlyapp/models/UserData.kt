package com.example.lendlyapp.models

data class UserData(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val street: String = "",
    val houseNumber: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: String = ""
)