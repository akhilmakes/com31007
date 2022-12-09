package com.example.week_5B_solution.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationClient(
    val context : Context,
    val client: FusedLocationProviderClient
) {

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    @SuppressLint("MissingPermission")
    fun receiveLocationUpdates(interval: Long): Flow<Location>{
        return callbackFlow {

            locationRequest = com.google.android.gms.location.LocationRequest.Builder(interval)
                .setMinUpdateIntervalMillis(interval)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.locations.lastOrNull()?.let{ location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose{
                 client.removeLocationUpdates(locationCallback)
            }

        }
    }
}