package com.example.week_5B_solution.view

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager.BadTokenException
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.week_5B_solution.R
import com.example.week_5B_solution.databinding.ActivityMainBinding
import com.example.week_5B_solution.model.CameraActivity
import com.example.week_5B_solution.model.ImageData
import com.example.week_5B_solution.model.LocationService
import com.example.week_5B_solution.viewmodel.AppViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // FOR MARKERS NOT FOR THE TRACKING

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private var appViewModel: AppViewModel? = null

    var pickFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        val photo_uri = result.data?.extras?.getString("uri")

        photo_uri?.let{
            val uri = Uri.parse(photo_uri)

            val imageData = ImageData(
                title = "Add Title Here",
                description = "Add Description Here",
                imagePath = uri.toString(),
                pathID = MapActivity.pathNumber!!
            )
            this.appViewModel!!.addImage(imageData, uri)


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        this.appViewModel!!.generateNewPath()

        checkButtonVisibility()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mainMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val startTrackingBtn = findViewById<Button>(R.id.startTrackingBtn)
        val goToTrackingPageBtn = findViewById<Button>(R.id.go_to_tracker_btn)

        startTrackingBtn.setOnClickListener{
            if(!locationPermissionsGranted()){
                requestLocationPermissions()
            } else {
                startLocationService()

                startTrackingBtn.isVisible = false
                goToTrackingPageBtn.isVisible = true
            }
        }



        goToTrackingPageBtn.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        val galleryFab: FloatingActionButton = findViewById(R.id.go_to_gallery_fab)

        galleryFab.setOnClickListener{
            val intent = Intent(applicationContext, GalleryActivity::class.java)
            startActivity(intent)
        }

        val cameraPickerFab: FloatingActionButton = findViewById(R.id.capture_image_fab_in_main)

        cameraPickerFab.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, CameraActivity::class.java)
            pickFromCamera.launch(intent)
        })

    }

    override fun onResume() {
        super.onResume()
        checkButtonVisibility()
    }


    fun checkButtonVisibility(){

        findViewById<Button>(R.id.startTrackingBtn).isVisible = !isLocationServiceRunning()
        findViewById<Button>(R.id.go_to_tracker_btn).isVisible = isLocationServiceRunning()
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



    override fun onMapReady(googleMap: GoogleMap) {
        // Main Page. Show markers here

        requestLocationPermissions()
        mMap = googleMap


        //mMap.isMyLocationEnabled = true
        // mMap.uiSettings.isMyLocationButtonEnabled = true

        val markerList = this.appViewModel!!.getOneLatLngFromPath()

        for (i in markerList) {
            Log.d("Marker", i.toString())

            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(i.lat, i.lng))
                    .title(i.title)
                    .snippet(i.pathID.toString())
            )

            mMap.setOnMarkerClickListener {marker->
                val intent = Intent(this, PathDetailActivity::class.java)

                intent.putExtra("title", marker.title)
                intent.putExtra("pathID", marker.snippet.toString().toInt())
                Log.d("Extra", marker.title!!)
                Log.d("Extra", marker.snippet!!)
                startActivity(intent)

                true
            }

        }

        val sheffield = LatLng(53.37, -1.462)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sheffield, 15f))
    }
}