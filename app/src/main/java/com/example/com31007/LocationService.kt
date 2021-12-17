package com.example.com31007

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception
import java.text.DateFormat
import java.util.*

class LocationService : Service {
    private var mCurrentLocation: Location? = null
    private var mLastUpdateTime: String? = null

    private var startMode: Int = 0             // indicates how to behave if the service is killed
    private var binder: IBinder? = null        // interface for clients that bind
    private var allowRebind: Boolean = false   // indicates whether onRebind should be used

    constructor(name: String?) : super() {}
    constructor() : super() {}

    override fun onCreate() {
        // The service is being created
        Log.e("Location Service", "onCreate finished")
    }


    @SuppressLint("LongLogTag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // The service is starting
        if (LocationResult.hasResult(intent!!)) {
            val locResults = LocationResult.extractResult(intent)
            for (location in locResults.locations) {
                if (location == null) continue
                //do something with the location
                Log.i("This is in service, New Location", "Current location: $location")
                mCurrentLocation = location
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                Log.i("This is in service, MAP", "new location " + mCurrentLocation.toString())
                // check if the activity has not been closed in the meantime
                if (MapsActivity.getActivity() != null) // any modification of the user interface must be done on the UI Thread. The Intent Service is running
                // in its own thread, so it cannot communicate with the UI.
                    MapsActivity.getActivity()?.runOnUiThread(Runnable {
                        try {
                            MapsActivity.getMap().addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        mCurrentLocation!!.latitude,
                                        mCurrentLocation!!.longitude
                                    )
                                )
                                    .title(mLastUpdateTime)
                            )
                            val zoom = CameraUpdateFactory.zoomTo(15f)
                            // it centres the camera around the new location
                            MapsActivity.getMap().moveCamera(
                                CameraUpdateFactory.newLatLng(
                                    LatLng(
                                        mCurrentLocation!!.latitude,
                                        mCurrentLocation!!.longitude
                                    )
                                )
                            )
                            // it moves the camera to the selected zoom
                            MapsActivity.getMap().animateCamera(zoom)
                        } catch (e: Exception) {
                            Log.e("LocationService", "Error cannot write on map " + e.message)
                        }
                    })
            }
        }
        return startMode
    }

    override fun onBind(intent: Intent): IBinder? {
        // A client is binding to the service with bindService()
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        // All clients have unbound with unbindService()
        return allowRebind
    }

    override fun onRebind(intent: Intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    override fun onDestroy() {
        // The service is no longer used and is being destroyed
        Log.e("Service", "end")
    }
}


