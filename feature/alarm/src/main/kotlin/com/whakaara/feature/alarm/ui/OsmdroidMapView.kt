package com.whakaara.feature.alarm.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.whakaara.feature.alarm.MapPickerMode
import com.whakaara.feature.alarm.SelectionEdge
import com.whakaara.feature.alarm.map.OsmdroidInitializer
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import kotlin.math.hypot
import kotlin.math.max

@Composable
fun OsmdroidMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    zoom: Double = 15.0,
    radiusMeters: Int = 0,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    OsmdroidInitializer.init(context)

    val mapView = remember {
        MapView(context).apply {
            configureMapDefaults(zoom)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onResume()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.setUseDataConnection(true)
            val center = GeoPoint(latitude, longitude)
            view.controller.setZoom(zoom)
            view.controller.setCenter(center)

            view.overlays.removeAll { overlay ->
                overlay is Marker || overlay is Polygon
            }

            if (radiusMeters > 0) {
                val circlePoints = buildCirclePoints(center, radiusMeters)
                val circle = Polygon().apply {
                    points = circlePoints
                    fillColor = 0x33D35400
                    strokeColor = 0xFFD35400.toInt()
                    strokeWidth = 3f
                }
                view.overlays.add(circle)
            }

            val marker = Marker(view)
            marker.position = center
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            view.overlays.add(marker)

            view.invalidate()
        },
    )
}

@Composable
fun OsmdroidMapPickerView(
    centerLatitude: Double,
    centerLongitude: Double,
    selectedLatitude: Double?,
    selectedLongitude: Double?,
    mode: MapPickerMode,
    rangeRadiusMeters: Int,
    widthMeters: Int,
    heightMeters: Int,
    onLocationPicked: (Double, Double) -> Unit,
    onEdgeExpand: (SelectionEdge) -> Unit,
    modifier: Modifier = Modifier,
    zoom: Double = 16.0,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnLocationPicked by rememberUpdatedState(onLocationPicked)
    val latestOnEdgeExpand by rememberUpdatedState(onEdgeExpand)

    OsmdroidInitializer.init(context)

    val mapView = remember {
        MapView(context).apply {
            configureMapDefaults(zoom)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onResume()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
        }
    }

    DisposableEffect(mapView) {
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(geoPoint: GeoPoint?): Boolean {
                geoPoint?.let { latestOnLocationPicked(it.latitude, it.longitude) }
                return true
            }

            override fun longPressHelper(geoPoint: GeoPoint?): Boolean = false
        }
        val eventsOverlay = MapEventsOverlay(receiver)
        mapView.overlays.add(0, eventsOverlay)
        onDispose {
            mapView.overlays.remove(eventsOverlay)
        }
    }

    DisposableEffect(mapView) {
        val selectionOverlay = ExactSelectionOverlay(
            onDragCenter = { lat, lng -> latestOnLocationPicked(lat, lng) },
            onEdgeExpand = { edge -> latestOnEdgeExpand(edge) },
        )
        mapView.overlays.add(selectionOverlay)
        onDispose {
            mapView.overlays.remove(selectionOverlay)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.setUseDataConnection(true)

            val centerKey = "center:$centerLatitude,$centerLongitude,$zoom"
            if (view.tag != centerKey) {
                view.tag = centerKey
                view.controller.setZoom(zoom)
                view.controller.setCenter(GeoPoint(centerLatitude, centerLongitude))
            }

            val pinLatitude = selectedLatitude ?: centerLatitude
            val pinLongitude = selectedLongitude ?: centerLongitude
            val pin = GeoPoint(pinLatitude, pinLongitude)

            view.overlays.removeAll { overlay -> overlay is Marker || overlay is Polygon }

            val exactOverlay = view.overlays
                .filterIsInstance<ExactSelectionOverlay>()
                .firstOrNull()

            when (mode) {
                MapPickerMode.Range -> {
                    exactOverlay?.updateSelection(
                        latitude = pinLatitude,
                        longitude = pinLongitude,
                        widthMeters = widthMeters,
                        heightMeters = heightMeters,
                        enabled = false,
                    )
                    if (rangeRadiusMeters > 0) {
                        val circle = Polygon().apply {
                            points = buildCirclePoints(pin, rangeRadiusMeters)
                            fillColor = 0x33D35400
                            strokeColor = 0xFFD35400.toInt()
                            strokeWidth = 3f
                        }
                        view.overlays.add(circle)
                    }
                    val marker = Marker(view).apply {
                        position = pin
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    view.overlays.add(marker)
                }
                MapPickerMode.Exact -> {
                    exactOverlay?.updateSelection(
                        latitude = pinLatitude,
                        longitude = pinLongitude,
                        widthMeters = widthMeters,
                        heightMeters = heightMeters,
                        enabled = true,
                    )
                }
            }

            view.invalidate()
        },
    )
}

/**
 * Handles dragging the selection square and tapping edge midpoints to double that side.
 */
private class ExactSelectionOverlay(
    private val onDragCenter: (Double, Double) -> Unit,
    private val onEdgeExpand: (SelectionEdge) -> Unit,
) : Overlay() {
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var widthMeters: Int = 200
    private var heightMeters: Int = 200
    private var enabled: Boolean = false

    private var dragging = false
    private val lastPoint = Point()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x22D35400
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFFD35400.toInt()
        strokeWidth = 5f
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFD35400.toInt()
    }
    private val handleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFFFFFFFF.toInt()
        strokeWidth = 3f
    }

    fun updateSelection(
        latitude: Double,
        longitude: Double,
        widthMeters: Int,
        heightMeters: Int,
        enabled: Boolean,
    ) {
        this.latitude = latitude
        this.longitude = longitude
        this.widthMeters = widthMeters
        this.heightMeters = heightMeters
        this.enabled = enabled
        if (!enabled) {
            dragging = false
        }
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow || !enabled) return
        val projection = mapView.projection
        val rect = selectionScreenRect(projection) ?: return

        canvas.drawRect(rect, fillPaint)
        canvas.drawRect(rect, strokePaint)

        edgeHandleCenters(rect).forEach { (point, _) ->
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), HANDLE_RADIUS_PX, handlePaint)
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), HANDLE_RADIUS_PX, handleStrokePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
        if (!enabled) return false
        val projection = mapView.projection
        val rect = selectionScreenRect(projection) ?: return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val hitEdge = hitTestEdge(event.x, event.y, rect)
                if (hitEdge != null) {
                    onEdgeExpand(hitEdge)
                    mapView.invalidate()
                    return true
                }
                if (rect.contains(event.x, event.y)) {
                    dragging = true
                    lastPoint.set(event.x.toInt(), event.y.toInt())
                    mapView.parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragging) return false
                val from = projection.fromPixels(lastPoint.x, lastPoint.y) as GeoPoint
                val to = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                val dLat = to.latitude - from.latitude
                val dLng = to.longitude - from.longitude
                latitude += dLat
                longitude += dLng
                lastPoint.set(event.x.toInt(), event.y.toInt())
                onDragCenter(latitude, longitude)
                mapView.invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragging) {
                    dragging = false
                    mapView.parent?.requestDisallowInterceptTouchEvent(false)
                    return true
                }
            }
        }
        return false
    }

    private fun selectionScreenRect(projection: Projection): RectF? {
        val center = GeoPoint(latitude, longitude)
        val points = buildRectPoints(center, widthMeters, heightMeters)
        if (points.size < 4) return null
        val screen = points.take(4).map { geo ->
            val p = Point()
            projection.toPixels(geo, p)
            p
        }
        val left = screen.minOf { it.x }.toFloat()
        val right = screen.maxOf { it.x }.toFloat()
        val top = screen.minOf { it.y }.toFloat()
        val bottom = screen.maxOf { it.y }.toFloat()
        // Keep a usable hit target even when zoomed out far
        val minSize = HANDLE_RADIUS_PX * 4f
        val width = max(right - left, minSize)
        val height = max(bottom - top, minSize)
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        return RectF(cx - width / 2f, cy - height / 2f, cx + width / 2f, cy + height / 2f)
    }

    private fun edgeHandleCenters(rect: RectF): List<Pair<Point, SelectionEdge>> {
        return listOf(
            Point(rect.centerX().toInt(), rect.top.toInt()) to SelectionEdge.North,
            Point(rect.centerX().toInt(), rect.bottom.toInt()) to SelectionEdge.South,
            Point(rect.right.toInt(), rect.centerY().toInt()) to SelectionEdge.East,
            Point(rect.left.toInt(), rect.centerY().toInt()) to SelectionEdge.West,
        )
    }

    private fun hitTestEdge(x: Float, y: Float, rect: RectF): SelectionEdge? {
        val hitRadius = HANDLE_HIT_RADIUS_PX
        return edgeHandleCenters(rect).firstOrNull { (point, _) ->
            hypot((x - point.x).toDouble(), (y - point.y).toDouble()) <= hitRadius
        }?.second
    }

    companion object {
        private const val HANDLE_RADIUS_PX = 14f
        private const val HANDLE_HIT_RADIUS_PX = 36.0
    }
}

private fun MapView.configureMapDefaults(zoom: Double) {
    setTileSource(TileSourceFactory.MAPNIK)
    setMultiTouchControls(true)
    isTilesScaledToDpi = true
    setUseDataConnection(true)
    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
    controller.setZoom(zoom)
}

private fun buildCirclePoints(center: GeoPoint, radiusMeters: Int): List<GeoPoint> {
    return (0 until 360 step 6).map { bearing ->
        center.destinationPoint(radiusMeters.toDouble(), bearing.toDouble())
    }
}

private fun buildRectPoints(center: GeoPoint, widthMeters: Int, heightMeters: Int): List<GeoPoint> {
    val halfW = widthMeters / 2.0
    val halfH = heightMeters / 2.0
    val north = center.destinationPoint(halfH, 0.0)
    val south = center.destinationPoint(halfH, 180.0)
    val northWest = north.destinationPoint(halfW, 270.0)
    val northEast = north.destinationPoint(halfW, 90.0)
    val southEast = south.destinationPoint(halfW, 90.0)
    val southWest = south.destinationPoint(halfW, 270.0)
    return listOf(northWest, northEast, southEast, southWest, northWest)
}
