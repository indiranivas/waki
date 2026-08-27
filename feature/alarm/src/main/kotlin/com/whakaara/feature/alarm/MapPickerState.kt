package com.whakaara.feature.alarm

import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt

enum class SelectionEdge {
    North,
    South,
    East,
    West,
}

enum class MapPickerMode {
    /** Classic pin + circular radius (slider). */
    Range,
    /** Draggable square with expandable sides. */
    Exact,
}

data class MapPickerState(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val selectedLatitude: Double? = null,
    val selectedLongitude: Double? = null,
    val mode: MapPickerMode = MapPickerMode.Range,
    /** Circular range radius used in [MapPickerMode.Range]. */
    val rangeRadiusMeters: Int = DEFAULT_RANGE_METERS,
    /** East-west span of the selection square/rectangle, in meters. */
    val widthMeters: Int = DEFAULT_SIDE_METERS,
    /** North-south span of the selection square/rectangle, in meters. */
    val heightMeters: Int = DEFAULT_SIDE_METERS,
    val isConfirming: Boolean = false,
) {
    val selectionLatitude: Double
        get() = selectedLatitude ?: centerLatitude

    val selectionLongitude: Double
        get() = selectedLongitude ?: centerLongitude

    /** Circular geofence radius that fully covers the selection rectangle. */
    val coveringRadiusMeters: Int
        get() {
            val halfW = widthMeters / 2.0
            val halfH = heightMeters / 2.0
            return hypot(halfW, halfH).roundToInt().coerceIn(MIN_SIDE_METERS, MAX_SIDE_METERS)
        }

    /** Radius applied to the alarm after confirm, based on the active mode. */
    val effectiveRadiusMeters: Int
        get() = when (mode) {
            MapPickerMode.Range -> rangeRadiusMeters.coerceIn(MIN_SIDE_METERS, MAX_SIDE_METERS)
            MapPickerMode.Exact -> coveringRadiusMeters
        }

    fun expanded(edge: SelectionEdge): MapPickerState {
        if (mode != MapPickerMode.Exact) return this

        val lat = selectionLatitude
        val lng = selectionLongitude
        val halfW = widthMeters / 2.0
        val halfH = heightMeters / 2.0

        return when (edge) {
            SelectionEdge.North -> {
                val newHeight = (heightMeters * 2).coerceAtMost(MAX_SIDE_METERS)
                val southLat = lat - metersToLatitudeOffset(halfH)
                val newCenterLat = southLat + metersToLatitudeOffset(newHeight / 2.0)
                copy(
                    selectedLatitude = newCenterLat,
                    selectedLongitude = lng,
                    heightMeters = newHeight,
                )
            }
            SelectionEdge.South -> {
                val newHeight = (heightMeters * 2).coerceAtMost(MAX_SIDE_METERS)
                val northLat = lat + metersToLatitudeOffset(halfH)
                val newCenterLat = northLat - metersToLatitudeOffset(newHeight / 2.0)
                copy(
                    selectedLatitude = newCenterLat,
                    selectedLongitude = lng,
                    heightMeters = newHeight,
                )
            }
            SelectionEdge.East -> {
                val newWidth = (widthMeters * 2).coerceAtMost(MAX_SIDE_METERS)
                val westLng = lng - metersToLongitudeOffset(halfW, lat)
                val newCenterLng = westLng + metersToLongitudeOffset(newWidth / 2.0, lat)
                copy(
                    selectedLatitude = lat,
                    selectedLongitude = newCenterLng,
                    widthMeters = newWidth,
                )
            }
            SelectionEdge.West -> {
                val newWidth = (widthMeters * 2).coerceAtMost(MAX_SIDE_METERS)
                val eastLng = lng + metersToLongitudeOffset(halfW, lat)
                val newCenterLng = eastLng - metersToLongitudeOffset(newWidth / 2.0, lat)
                copy(
                    selectedLatitude = lat,
                    selectedLongitude = newCenterLng,
                    widthMeters = newWidth,
                )
            }
        }
    }

    companion object {
        const val DEFAULT_RANGE_METERS = 200
        const val DEFAULT_SIDE_METERS = 200
        const val MIN_SIDE_METERS = 100
        const val MAX_SIDE_METERS = 5000

        fun metersToLatitudeOffset(meters: Double): Double = meters / 111_320.0

        fun metersToLongitudeOffset(meters: Double, latitude: Double): Double {
            val metersPerDegree = 111_320.0 * cos(Math.toRadians(latitude)).coerceAtLeast(0.01)
            return meters / metersPerDegree
        }
    }
}
