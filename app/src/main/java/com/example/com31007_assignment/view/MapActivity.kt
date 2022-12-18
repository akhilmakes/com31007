package com.example.com31007_assignment.view

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.com31007_assignment.ImageApplication
import com.example.com31007_assignment.R
import com.example.com31007_assignment.viewmodel.AppViewModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.com31007_assignment.model.*
import com.example.com31007_assignment.databinding.ActivityMapsBinding
import com.google.android.gms.common.config.GservicesValue.value
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*


/**
 * This activity is for the live tracking of the path once the user has started the tracking service.
 * This is done by adding updated Polylines to the map.
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback{


    private lateinit var latLngForPath: List<LatLngData>

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var appViewModel: AppViewModel? = null

    private lateinit var dbLatLngDataDao: LatLngDataDao
    private lateinit var dbPathDao : PathDao

    /**
     * This function initialises that data access objects, that will be used to query the database.
     */

    private fun initDataDao(){
        dbLatLngDataDao = (this@MapActivity.application as ImageApplication)
            .databaseObj.latLngDataDao()
        dbPathDao = (this@MapActivity.application as ImageApplication)
            .databaseObj.pathDao()
    }


    //region ActivityResultContract


    var pickFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        val photo_uri = result.data?.extras?.getString("uri")

        photo_uri?.let{
            val uri = Uri.parse(photo_uri)

            val input = contentResolver.openInputStream(uri)!!

            val exif = ExifInterface(input)

            var lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
            var long = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
            val longRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

            val date = exif.getAttribute(ExifInterface.TAG_DATETIME)

            if(!lat.isNullOrEmpty()) {
                lat = parseLatLng(lat)
            }
            if(!long.isNullOrEmpty()){
                long = parseLatLng(long)
            }

            val latLng = if (longRef == "W"){
                "$lat, -$long"
            } else {
                "$lat, $long"
            }

            val airPressure = this.appViewModel!!.getLatestPressure()


            val imageData = ImageData(
                title = "Add Title Here",
                description = "Add Description Here",
                imagePath = uri.toString(),
                pathID = pathNumber!!,
                latLng = latLng,
                airPressure = airPressure,
                dateTime = date
            )
            this.appViewModel!!.addImage(imageData, uri)


        }
    }

    /**
     * This function is used to convert the latitude and longitude string produced by the Exif tag
     * to a readable format to be displayed on the image display activities.
     *
     * @param exifTag is the tag to be parsed into a readable format
     *
     * @return This function returns a string which is a readable form of the input.
     */
    private fun parseLatLng(exifTag: String): String{

        val degrees = exifTag.substring(0, exifTag.indexOf("/"))

        val minutes = exifTag.substring(degrees.length-1, exifTag.indexOf("/"))

        val seconds = exifTag.substring(minutes.length-1, exifTag.indexOf("/"))

        val result = degrees.toDouble() + (minutes.toDouble()/60) + (seconds.toDouble()/3600)

        return result.toString()

    }

    //endregion ActivityResultContract

    //region Overridden functions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]


        this.appViewModel!!.retrieveCurrentPath().observe(this, Observer { currentPath ->

            pathNumber = currentPath
        })

        initDataDao()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(  R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val cameraPickerFab: FloatingActionButton = findViewById(R.id.capture_image_fab)

        cameraPickerFab.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, CameraActivity::class.java)
            pickFromCamera.launch(intent)
        })

        //cameraPickerFab.hide()
        val stopTrackingButton: Button = findViewById(R.id.stopTrackingBtn)
        stopTrackingButton.setOnClickListener {
            val pathInfo = this.appViewModel!!.getPathForID(pathNumber!!)
            stopTrackingButton.isVisible = false
            stopTrackingService()

            val intent = Intent(this, PathDetailActivity::class.java)

            intent.putExtra("title", pathInfo.title)
            intent.putExtra("pathID", pathNumber)

            startActivity(intent)

        }

    }

    override fun onRestart() {
        super.onRestart()
        addPolylineToMap()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        // Do live tracking here
        mMap = googleMap

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true


        addPolylineToMap()


        // Add a marker in Sydney and move the camera
        val sheffield = LatLng(53.37, -1.462)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sheffield))
    }

    //endregion Overridden functions

    //region Tracking service functions

    /**
     * This function is used to check if the tracking service is running.
     *
     * @return true is tracking service is running.
     */
    private fun trackingServiceRunning(): Boolean {

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for(service in activityManager.getRunningServices(Int.MAX_VALUE)){
            if(TrackingService::class.java.name.equals(service.service.className)){
                if(service.foreground) return true
            }
        }
        return false

    }

    /**
     * This function is used to stop the foreground activity by creating an intent and setting its
     * action to ACTION_STOP. It displays a Toast message informing the user the service has
     * stopped.
     */
    private fun stopTrackingService() {
        if(trackingServiceRunning()){
            val intent = Intent(
                applicationContext,
                TrackingService::class.java
            ).setAction(TrackingService.ACTION_STOP)
            startService(intent)
            Toast.makeText(this, "Stopping Tracking Service", Toast.LENGTH_SHORT).show()

        }
    }

    //endregion Tracking service functions

    //region Path drawing functions

    fun addPolylineToMap(){

        if (pathNumber != null){
            mMap.addPolyline(drawPath())
        }

    }

    fun drawPath():PolylineOptions{

        val polylineOptions = PolylineOptions()

        if (pathNumber != null){
            latLngForPath = this.appViewModel!!.getAllLatLng(pathNumber!!)

            for(latLng in latLngForPath){

                polylineOptions.add(LatLng(latLng.lat,latLng.lng))

            }
        }

        return polylineOptions

    }

    //endregion Path drawing functions


    companion object{
        var pathNumber: Int? = null
    }
}