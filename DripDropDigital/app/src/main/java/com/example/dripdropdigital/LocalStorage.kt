package com.example.dripdropdigital

import android.content.Context

object LocalStorage {

    private const val PREF_NAME = "my_app_preferences"
    private const val KEY_PLANT = "plant"

    fun addPlant(context: Context, plant: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val currentPlants = getPlants(context)
        val newPlants = if (currentPlants != null) {
            "$currentPlants;$plant"
        } else {
            plant
        }
        editor.putString(KEY_PLANT, newPlants)
        editor.apply()
    }

    fun getPlants(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_PLANT, null)
    }

    fun deleteLocations(context: Context){
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_PLANT)
        editor.apply()
    }

}
