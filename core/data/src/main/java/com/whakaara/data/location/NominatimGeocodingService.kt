package com.whakaara.data.location

import com.whakaara.core.location.GeocodingService
import com.whakaara.core.location.LocationDistanceUtils
import com.whakaara.model.location.Place
import javax.inject.Inject

class NominatimGeocodingService @Inject constructor(
    private val api: NominatimApi
) : GeocodingService {
    override suspend fun search(query: String, userLatitude: Double?, userLongitude: Double?): List<Place> {
        val viewbox = if (userLatitude != null && userLongitude != null) {
            LocationDistanceUtils.viewboxAround(userLatitude, userLongitude)
        } else {
            null
        }

        val results = api.search(
            query = query,
            viewbox = viewbox,
            bounded = if (viewbox != null) 0 else null
        )

        val places = results.map { result ->
            val latitude = result.lat.toDouble()
            val longitude = result.lon.toDouble()
            val distance = if (userLatitude != null && userLongitude != null) {
                LocationDistanceUtils.formatDistance(
                    LocationDistanceUtils.distanceMeters(
                        userLatitude,
                        userLongitude,
                        latitude,
                        longitude
                    )
                )
            } else {
                null
            }

            Place(
                name = result.displayName.split(",").firstOrNull()?.trim() ?: result.displayName,
                address = result.displayName,
                latitude = latitude,
                longitude = longitude,
                distance = distance
            )
        }

        return if (userLatitude != null && userLongitude != null) {
            places.sortedBy {
                LocationDistanceUtils.distanceMeters(
                    userLatitude,
                    userLongitude,
                    it.latitude,
                    it.longitude
                )
            }
        } else {
            places
        }
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Place? {
        val result = api.reverse(latitude, longitude)
        return Place(
            name = result.displayName.split(",").firstOrNull()?.trim() ?: result.displayName,
            address = result.displayName,
            latitude = latitude,
            longitude = longitude
        )
    }
}
