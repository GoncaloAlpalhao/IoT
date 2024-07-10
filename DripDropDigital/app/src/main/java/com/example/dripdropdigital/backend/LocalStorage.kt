package com.example.dripdropdigital.backend

import android.content.Context

/**
 * This class is responsible for managing the local storage of plant data in the application
 */
object LocalStorage {

    private const val PREF_NAME = "my_app_preferences"
    private const val KEY_PLANT = "plant"

    /**
     * Adds a plant to the list of stored plants
     *
     * @param context The context of the activity
     * @param plant The plant data to add
     */
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

    /**
     * Retrieves the list of stored plants
     *
     * @param context The context of the activity
     * @return A list of plant data strings, or null if no plants are stored
     */
    fun getPlants(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_PLANT, null)
    }

    /**
     * Delete all stored plant data
     *
     * @param context The context of the activity
     */
    fun deleteLocations(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_PLANT)
        editor.apply()
    }

}
