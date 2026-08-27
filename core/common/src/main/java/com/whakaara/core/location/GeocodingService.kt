package com.whakaara.core.location

import com.whakaara.model.location.Place

interface GeocodingService {
    suspend fun search(query: String, userLatitude: Double?, userLongitude: Double?): List<Place>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Place?
}
