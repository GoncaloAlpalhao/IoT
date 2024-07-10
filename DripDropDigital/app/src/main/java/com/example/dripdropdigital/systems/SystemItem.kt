package com.example.dripdropdigital.systems

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Represents system item data
 */
data class SystemItem(
    @SerializedName("title") val title: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("ip_address") val ipAddress: String?,
    @SerializedName("client_id") val clientId: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("last_will_topic") val lastWillTopic: String?,
    @SerializedName("last_will_payload") val lastWillPayload: String?,
    @SerializedName("min_humidity") val minHumidity: String?,
    @SerializedName("max_humidity") val maxHumidity: String?,
    @SerializedName("plant_type") val plantType: String?
) : Serializable
