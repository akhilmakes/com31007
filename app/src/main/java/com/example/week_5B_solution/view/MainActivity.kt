package com.example.week_5B_solution.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.week_5B_solution.R
import com.example.week_5B_solution.databinding.ActivityMainBinding
import com.example.week_5B_solution.viewmodel.AppViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // FOR MARKERS NOT FOR THE TRACKING

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private var appViewModel: AppViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        val startTrackingBtn = findViewById<Button>(R.id.startTrackingBtn)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mainMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!locationPermissionsGranted()){
            requestLocationPermissions()

        } else {
            startTrackingBtn.setOnClickListener {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
            }
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