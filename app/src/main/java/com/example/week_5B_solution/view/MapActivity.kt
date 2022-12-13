package com.example.week_5B_solution.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.week_5B_solution.GalleryActivity
import com.example.week_5B_solution.ImageApplication
import com.example.week_5B_solution.PathDetailActivity
import com.example.week_5B_solution.model.LocationService
import com.example.week_5B_solution.viewmodel.LocationViewModel
import com.example.week_5B_solution.R
import com.example.week_5B_solution.data.LatLngDataDao
import com.example.week_5B_solution.data.LocationTitle
import com.example.week_5B_solution.data.PathDao

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.week_5B_solution.databinding.ActivityMapsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var locationViewModel: LocationViewModel? = null

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

        this.locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        initDataDao()
        // myLatDataset.add(LatData(lat = 33.2, lng = 45.6))


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val controlLocationBtn: Button = findViewById(R.id.control_location_service_btn)

        controlLocationBtn.setOnClickListener{ button ->

            if(controlLocationBtn.text == getString(R.string.start)) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != GalleryActivity.GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != GalleryActivity.GRANTED
                ){
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                        GalleryActivity.REQUEST_CODE_LOCATION_PERMISSION
                    )
                } else {
                    controlLocationBtn.text  = getString(R.string.stop)
                    startLocationService()

                    this.locationViewModel!!.generateNewPath()


                }

            } else if (controlLocationBtn.text == getString(R.string.stop)) {
                controlLocationBtn.text = getString(R.string.start)
                stopLocationService()
            }

        }

        val galleryFab: FloatingActionButton = findViewById(R.id.go_to_gallery_fab)

        galleryFab.setOnClickListener{
            val intent = Intent(applicationContext, GalleryActivity::class.java)
            startActivity(intent)
        }
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

    private fun returnPath() {

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true


       var markerList = dbLatLngDataDao.getOnePathData()

        markerList.observe(this) { value ->
            for (i in value) {
                Log.d("Marker", i.toString())
                //Log.d("Marker", i.title)
//               Log.d("Marker", i.pathID.toString())
                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(i.lat, i.lng))
                        .title(i.title)
                        .snippet(i.pathID.toString())
                )

                mMap.setOnMarkerClickListener {marker->
                    var intent = Intent(this, PathDetailActivity::class.java)
                    //Log.d("Extra", i.toString())
                    intent.putExtra("title", marker.title)
                    intent.putExtra("pathID", marker.snippet.toString().toInt())
                    Log.d("Extra", marker.title!!)
                    Log.d("Extra", marker.snippet!!)
                    startActivity(intent)

                    true
                }

            }
        }


        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.addMarker(MarkerOptions().position(LatLng(53/1,23/1,1671468/100000,
//            1/1,28/1,4375596/100000)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

    }
}