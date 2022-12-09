package com.example.week_5B_solution.model

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.week_5B_solution.ImageApplication
import com.example.week_5B_solution.R
import com.example.week_5B_solution.data.LatData
import com.example.week_5B_solution.data.LatDataDao
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*



class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var locationClient: LocationClient

    private var dbLatDataDao: LatDataDao? = null

    init {

        dbLatDataDao = (this@LocationService.application as ImageApplication)
            .databaseObj.latDataDao()

    }




    override fun onCreate() {
        super.onCreate()

         locationClient = LocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
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
            .setContentText("Location: null, null")
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


         locationClient.receiveLocationUpdates(20000)
            .catch { e -> e.printStackTrace() }
            .onEach{ location ->
                val lat = location.latitude
                val long = location.longitude

                dbLatDataDao!!.insert(LatData(lat = lat, lng = long, pathNum = 1))

                val updatedNotification = builder.setContentText("Location: $lat, $long")

                notificationManager.notify(LOCATION_SERVICE_ID, updatedNotification.build())

            }
            .launchIn(serviceScope)

        startForeground(LOCATION_SERVICE_ID, builder.build())

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun stopLocationService(){
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

        var currentLocation: Location? = null

    }
}