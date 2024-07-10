package com.example.dripdropdigital.systems

/**
 * Represent a type of plant with its humidity requirements
 */
data class PlantType(val name: String, val minHumidity: Double, val maxHumidity: Double)