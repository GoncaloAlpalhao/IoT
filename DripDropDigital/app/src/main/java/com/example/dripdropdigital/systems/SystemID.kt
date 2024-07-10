package com.example.dripdropdigital.systems

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Represents the system information
 */
data class SystemID(
    @SerializedName("image") val image: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("id") val id: Int?
) : Serializable
