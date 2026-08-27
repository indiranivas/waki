package com.whakaara.data.location

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NominatimApi {
    @GET("search?format=json&addressdetails=1&limit=20")
    suspend fun search(
        @Query("q") query: String,
        @Query("viewbox") viewbox: String? = null,
        @Query("bounded") bounded: Int? = null,
        @Header("User-Agent") userAgent: String = "Waki-Android-App"
    ): List<NominatimSearchResult>

    @GET("reverse?format=json")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Header("User-Agent") userAgent: String = "Waki-Android-App"
    ): NominatimSearchResult
}

data class NominatimSearchResult(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("address") val address: NominatimAddress?
)

data class NominatimAddress(
    @SerializedName("road") val road: String?,
    @SerializedName("suburb") val suburb: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("postcode") val postcode: String?,
    @SerializedName("country") val country: String?
)
