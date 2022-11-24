package com.example.week_5B_solution

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocationService: Service() {


    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(result.lastLocation != null){
                val lat = result.lastLocation!!.latitude
                val long = result.lastLocation!!.longitude

                Log.d("LocationService", "$lat, $long")


            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnspecifiedImmutableFlag", "MissingPermission")
    private fun startLocationService(){
        val channelID = "location_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val resultIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            channelID
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Location Service")
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentText("Running")
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            if (notificationManager.getNotificationChannel(channelID) == null){

                val notificationChannel = NotificationChannel(
                    channelID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "This channel is used by the location service"
                notificationManager.createNotificationChannel(notificationChannel)

            }
        }


        val locationRequest = LocationRequest.Builder(4000)
            .setMinUpdateIntervalMillis(2000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        startForeground(LOCATION_SERVICE_ID, builder.build())

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(locationCallback)
        stopForeground(LOCATION_SERVICE_ID)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null){
            val action = intent.action

            if(action != null){
                if(action == ACTION_START){
                    startLocationService()
                } else if(action == ACTION_STOP){
                    stopLocationService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object{
        val LOCATION_SERVICE_ID = 175
        val ACTION_START = "startActionService"
        val ACTION_STOP = "stopActionService"
        
    }
}