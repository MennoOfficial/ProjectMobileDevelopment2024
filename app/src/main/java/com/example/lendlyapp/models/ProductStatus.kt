package com.example.lendlyapp.models

enum class ProductStatus(val displayName: String) {
    AVAILABLE("Available"),
    RENTED("Rented"),
    UNAVAILABLE("Unavailable"),
    ELECTRONICS("Electronics"),
    GARDEN_TOOLS("Garden Tools"),
    SPORTS_EQUIPMENT("Sports Equipment"),
    HOME_APPLIANCES("Home Appliances"),
    BOOKS("Books"),
    MUSIC_INSTRUMENTS("Music Instruments"),
    CAMPING_GEAR("Camping Gear"),
    PARTY_SUPPLIES("Party Supplies"),
    TOOLS("Tools"),
    GAMES("Games");

    companion object {
        fun getAllTags(): List<String> = values()
            .filter { it !in listOf(AVAILABLE, RENTED, UNAVAILABLE) }
            .map { it.displayName }
            
        fun isStatus(value: String): Boolean {
            return value.uppercase() in listOf(AVAILABLE.name, RENTED.name, UNAVAILABLE.name)
        }
    }
} 