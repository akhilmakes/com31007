package com.example.com31007_assignment.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.ViewModelProvider
import com.example.com31007_assignment.R
import com.example.com31007_assignment.databinding.ActivityMainBinding
import com.example.com31007_assignment.model.ImageData
import com.example.com31007_assignment.model.TrackingService
import com.example.com31007_assignment.viewmodel.AppViewModel
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

    /**
     * This function checks if the tracking service is running and controls the visibility of the
     * start tracking and current path button.
     */
    private fun checkButtonVisibility(){

        findViewById<Button>(R.id.startTrackingBtn).isVisible = !trackingServiceRunning()
        findViewById<Button>(R.id.go_to_tracker_btn).isVisible = trackingServiceRunning()
    }

    //region ActivityResultContract

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

    //endregion ActivityResultContract

    //region Overridden Functions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        // this.appViewModel!!.generateNewPath()

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
                startTrackingService()
                this.appViewModel!!.generateNewPath()
                startTrackingBtn.isVisible = false
                // goToTrackingPageBtn.isVisible = true
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)

            }
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

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        // Main Page. Show markers here

        requestLocationPermissions()
        mMap = googleMap


        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

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

    //endregion Overridden Functions

    //region Permissions functions

    private fun locationPermissionsGranted() = (ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_COARSE_LOCATION) == GalleryActivity.GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == GalleryActivity.GRANTED)


    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            GalleryActivity.REQUEST_CODE_LOCATION_PERMISSION)


    }

    //endregion Permissions functions

    //region Tracking service functions

    /**
     * This function is used to start the foreground activity by creating an intent and setting its
     * action to ACTION_START. It displays a Toast message informing the user the service has
     * started.
     */
    private fun startTrackingService() {
        if(!trackingServiceRunning()){
            val intent = Intent(
                applicationContext,
                TrackingService::class.java
            ).setAction(TrackingService.ACTION_START)
            startService(intent)
            Toast.makeText(this, "Starting Tracking Service", Toast.LENGTH_SHORT).show()
        }
    }

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

    //endregion Tracking service functions


}