package com.example.com31007_assignment.model

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.com31007_assignment.R
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.com31007_assignment.ImageApplication
import com.example.com31007_assignment.model.*
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*


class TrackingService: Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var sensorManager: SensorManager
    private lateinit var barometerSensor: Sensor


    private lateinit var dbLatLngDataDao: LatLngDataDao
    private lateinit var dbPathDao: PathDao



    override fun onCreate() {
        super.onCreate()

        initDataDao()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager


        locationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
    }



    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnspecifiedImmutableFlag", "MissingPermission")
    private fun startTrackingService(){

        val channelID = "location_notification_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val resultIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            channelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracking Service:")
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentText("Location: null, null\nAir Pressure: null")
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            if (notificationManager.getNotificationChannel(channelID) == null){

                val notificationChannel = NotificationChannel(
                    channelID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.description = "This channel is used by the tracking service"
                notificationManager.createNotificationChannel(notificationChannel)

            }
        }

        if(sensorAvailable(Sensor.TYPE_PRESSURE)){
            barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

            sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        getLocationUpdates()
            .catch { e -> e.printStackTrace() }
            .onEach{ location ->
                val lat = location.latitude
                val long = location.longitude

                val currentPath = dbPathDao.getCurrentPathNum()

                dbLatLngDataDao.insert(LatLngData(lat = lat, lng = long, airPressure = currentAirPressure, pathID = currentPath))

                val updatedNotification = builder.setContentText("Location: $lat, $long\n Air Pressure: $currentAirPressure mBar")

                notificationManager.notify(TRACKING_SERVICE_ID, updatedNotification.build())

            }
            .launchIn(serviceScope)

        startForeground(TRACKING_SERVICE_ID, builder.build())

    }

    fun sensorAvailable(sensor: Int): Boolean{

        val sensorMan = getSystemService(SENSOR_SERVICE) as SensorManager

        for (i in sensorMan.getSensorList(Sensor.TYPE_ALL)){
            if (i.type == sensor){
                Log.d("SENSOR", i.toString())
                return true
            }
        }

        return false

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun stopTrackingService(){
        sensorManager.unregisterListener(this)
        stopForeground(TRACKING_SERVICE_ID)
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun getLocationUpdates(): Flow<Location>{
        return callbackFlow {

            locationRequest = LocationRequest.Builder(20000)
                .setMinUpdateIntervalMillis(20000)
                .setPriority(Priority.PRIORITY_LOW_POWER).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.locations.lastOrNull()?.let{ location ->
                        launch { send(location) }
                    }
                }
            }

            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose{
                locationClient.removeLocationUpdates(locationCallback)
            }

        }

    }

    override fun onSensorChanged(event: SensorEvent?) {

        currentAirPressure = event!!.values[0]
        Log.d("SENSOR", "${event.values[0]}")


    }

    override fun onAccuracyChanged(sensor: Sensor?, value: Int) {


    }




    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null){
            val action = intent.action

            if(action != null){
                if(action == ACTION_START){
                    startTrackingService()
                } else if(action == ACTION_STOP){
                    stopTrackingService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun initDataDao(){
        dbPathDao = (this@TrackingService.application as ImageApplication)
            .databaseObj.pathDao()
        dbLatLngDataDao = (this@TrackingService.application as ImageApplication)
            .databaseObj.latLngDataDao()
    }

    companion object{
        val TRACKING_SERVICE_ID = 175
        val ACTION_START = "startActionService"
        val ACTION_STOP = "stopActionService"

        val interval : Long = 20000

        var currentAirPressure: Float? = null

        var currentPath: Path? = null

    }


}