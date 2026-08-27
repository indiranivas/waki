package com.whakaara.core.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationDistanceUtils {
    fun distanceMeters(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double
    ): Double {
        val earthRadius = 6371000.0
        val lat1 = fromLatitude.toRadians()
        val lat2 = toLatitude.toRadians()
        val deltaLat = (toLatitude - fromLatitude).toRadians()
        val deltaLon = (toLongitude - fromLongitude).toRadians()

        val a = sin(deltaLat / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    fun formatDistance(meters: Double): String {
        val miles = meters / 1609.344
        return if (miles < 0.1) {
            String.format("%.0f ft", meters * 3.28084)
        } else {
            String.format("%.1f miles", miles)
        }
    }

    fun viewboxAround(latitude: Double, longitude: Double, deltaDegrees: Double = 0.35): String {
        val minLon = longitude - deltaDegrees
        val maxLon = longitude + deltaDegrees
        val minLat = latitude - deltaDegrees
        val maxLat = latitude + deltaDegrees
        return "$minLon,$maxLat,$maxLon,$minLat"
    }

    private fun Double.toRadians(): Double = this * Math.PI / 180.0
}
