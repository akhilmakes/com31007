package com.example.week_5B_solution.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.week_5B_solution.ImageApplication
import com.example.week_5B_solution.viewmodel.AppViewModel
import com.example.week_5B_solution.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.week_5B_solution.databinding.ActivityMapsBinding
import com.example.week_5B_solution.model.*
import com.google.android.gms.common.config.GservicesValue.value
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback{

    // FOR THE TRACKING HERE

    private lateinit var latLngForPath: List<LatLngData>

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var appViewModel: AppViewModel? = null


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


            val imageData = ImageData(
                title = "Add Title Here",
                description = "Add Description Here",
                imagePath = uri.toString(),
                pathID = pathNumber!!,
                latLng = latLng,
                dateTime = date
            )
            this.appViewModel!!.addImage(imageData, uri)


        }
    }

    fun parseLatLng(exifTag: String): String{

        val degrees = exifTag.substring(0, exifTag.indexOf("/"))

        val minutes = exifTag.substring(degrees.length-1, exifTag.indexOf("/"))

        val seconds = exifTag.substring(minutes.length-1, exifTag.indexOf("/"))

        val result = degrees.toDouble() + (minutes.toDouble()/60) + (seconds.toDouble()/3600)

        return result.toString()

    }

    private lateinit var dbLatLngDataDao: LatLngDataDao
    private lateinit var dbPathDao : PathDao


    private fun initDataDao(){
        dbLatLngDataDao = (this@MapActivity.application as ImageApplication)
            .databaseObj.latLngDataDao()
        dbPathDao = (this@MapActivity.application as ImageApplication)
                .databaseObj.pathDao()
    }


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
            .findFragmentById(R.id.map) as SupportMapFragment
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
            stopLocationService()

            val intent = Intent(this, PathDetailActivity::class.java)

            intent.putExtra("title", pathInfo.title)
            intent.putExtra("pathID", pathNumber)

            startActivity(intent)

//            val goToMainPageBtn = findViewById<Button>(R.id.go_to_main_page)
//            goToMainPageBtn.setOnClickListener {
//
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//            }
        }

    }


    fun addPolylineToMap(){

        if (pathNumber != null){
            mMap.addPolyline(drawPath())
        }

    }


    override fun onRestart() {
        super.onRestart()

        addPolylineToMap()
    }



    private fun locationPermissionsGranted() = (ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_COARSE_LOCATION) == GalleryActivity.GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == GalleryActivity.GRANTED)


    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            GalleryActivity.REQUEST_CODE_LOCATION_PERMISSION)

    }


    private fun isLocationServiceRunning(): Boolean {

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for(service in activityManager.getRunningServices(Int.MAX_VALUE)){
            if(LocationService::class.java.name.equals(service.service.className)){
                if(service.foreground) return true
            }
        }
        return false

    }

    private fun startLocationService() {
        if(!isLocationServiceRunning()){
            val intent = Intent(
                applicationContext,
                LocationService::class.java
            ).setAction(LocationService.ACTION_START)
            startService(intent)
            Toast.makeText(this, "Starting Location Tracking", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationService() {
        if(isLocationServiceRunning()){
            val intent = Intent(
                applicationContext,
                LocationService::class.java
            ).setAction(LocationService.ACTION_STOP)
            startService(intent)
            Toast.makeText(this, "Stopping Location Tracking", Toast.LENGTH_SHORT).show()

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

    companion object{
        var pathNumber: Int? = null
    }
}