package com.whakaara.data.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.whakaara.core.LogUtils.logE
import com.whakaara.core.constants.GeneralConstants
import com.whakaara.model.location.LocationAlarm
import com.whakaara.model.location.LocationTriggerType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent().apply {
            setClassName(context.packageName, GeneralConstants.LOCATION_ALARM_RECEIVER)
            action = GeneralConstants.LOCATION_ALARM_TRIGGER_ACTION
        }
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(locationAlarm: LocationAlarm) {
        val radius = locationAlarm.radiusMeters.coerceAtLeast(100).toFloat()
        val geofence = Geofence.Builder()
            .setRequestId(locationAlarm.id.toString())
            .setCircularRegion(
                locationAlarm.latitude,
                locationAlarm.longitude,
                radius
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                if (locationAlarm.triggerType == LocationTriggerType.ARRIVE) {
                    Geofence.GEOFENCE_TRANSITION_ENTER
                } else {
                    Geofence.GEOFENCE_TRANSITION_EXIT
                }
            )
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent)
            .addOnFailureListener { exception ->
                logE(
                    message = "Failed to register geofence for alarm ${locationAlarm.id}",
                    throwable = exception
                )
            }
    }

    fun removeGeofence(locationAlarmId: String) {
        geofencingClient.removeGeofences(listOf(locationAlarmId))
            .addOnFailureListener { exception ->
                logE(
                    message = "Failed to remove geofence for alarm $locationAlarmId",
                    throwable = exception
                )
            }
    }

    @SuppressLint("MissingPermission")
    fun recreateGeofences(locationAlarms: List<LocationAlarm>) {
        val enabledAlarms = locationAlarms.filter { it.enabled }
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnCompleteListener {
                enabledAlarms.forEach { addGeofence(it) }
            }
    }
}
