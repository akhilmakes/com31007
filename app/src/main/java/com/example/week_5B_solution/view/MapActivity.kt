package com.example.week_5B_solution.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    // FOR THE TRACKING HERE

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var appViewModel: AppViewModel? = null

    private lateinit var locationClient: LocationClient

    private val controlLocationBtn: Button = findViewById(R.id.control_location_service_btn)


    var pickFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        val photo_uri = result.data?.extras?.getString("uri")

        photo_uri?.let{
            val uri = Uri.parse(photo_uri)

            val imageData = ImageData(
                title = "Add Title Here",
                description = "Add Description Here",
                imagePath = uri.toString(),
                pathID = pathNumber!!
            )
            this.appViewModel!!.addImage(imageData, uri)


        }
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

        locationClient = LocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )


        this.appViewModel!!.retrieveCurrentPath().observe(this, Observer {
            currentPath ->

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

        cameraPickerFab.hide()

        controlLocationBtn.setOnClickListener{ button ->

            if(controlLocationBtn.text == getString(R.string.start)) {
                if (!locationPermissionsGranted()){
                    requestLocationPermissions()

                } else {
                    controlLocationBtn.text  = getString(R.string.stop)
                    startLocationService()
                    cameraPickerFab.show()

                    this.appViewModel!!.generateNewPath()

                }

            } else if (controlLocationBtn.text == getString(R.string.stop)) {
                controlLocationBtn.text = getString(R.string.start)
                stopLocationService()
                cameraPickerFab.hide()
            }

        }

        val galleryFab: FloatingActionButton = findViewById(R.id.go_to_gallery_fab)

        galleryFab.setOnClickListener{
            val intent = Intent(applicationContext, GalleryActivity::class.java)
            startActivity(intent)
        }
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


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        // Do live tracking here

        requestLocationPermissions()

        mMap = googleMap
        val polylineOptions = PolylineOptions()

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true



        locationClient.receiveLocationUpdates(20000)
            .catch { e -> e.printStackTrace() }
            .onEach{ location ->
                val lat = location.latitude
                val long = location.longitude
                polylineOptions.add(LatLng(lat, long))

                mMap.addPolyline(polylineOptions)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, long), 15f))
            }

        // Add a marker in Sydney and move the camera
        val sheffield = LatLng(53.37, -1.462)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sheffield))
    }

    companion object{
        var pathNumber: Int? = null
    }
}