package com.example.week_5B_solution

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.week_5B_solution.data.ImageData
import com.example.week_5B_solution.data.ImageDataDao
import com.example.week_5B_solution.data.LatData
import com.example.week_5B_solution.data.LatDataDao

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.week_5B_solution.databinding.ActivityMapsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var imagedaoObj: ImageDataDao
    private lateinit var latdaoObj: LatDataDao
    // private var myImageDataset: MutableList<ImageData> = ArrayList<ImageData>()
    var myLatDataset: MutableList<LatData> = ArrayList<LatData>()



    private fun initData() {
//        imagedaoObj = (this@MapsActivity.application as ImageApplication)
//            .databaseObj.imageDataDao()
        latdaoObj = (this@MapsActivity.application as ImageApplication)
            .databaseObj.latDataDao()
        // myLatDataset.add(LatData(lat = 33.2, lng = 45.6))
        runBlocking {
            launch(Dispatchers.Default) {
                // myImageDataset.addAll(imagedaoObj.getItems())
                myLatDataset.addAll(latdaoObj.getLatLng())
                Log.d("TREE", myLatDataset.toString())
            }
        }
    }

    //[lat, lng]
    private suspend fun initNewLatData(data: MutableList<LatData>): LatData {

        var latData = LatData(
            lat = data[0].toString().toDouble(),
            lng = data[1].toString().toDouble()
        )
        latdaoObj?.let {
            coroutineScope{
                val insertJob = async(Dispatchers.IO){
                    // Insert the newly created ImageData entity
                    latdaoObj.insert(latData)
                }

                val rowId = insertJob.await().toString().toInt()

                // Using the rowId, retrieve the ImageData object from db,
                // or just update the id in the existing object
                // imageData.id = rowId
                val retrieveJob = async(Dispatchers.IO) {
                    latdaoObj.getItem(rowId)
                }
                latData = retrieveJob.await()

            }
        }
        return latData
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        initData()

        myLatDataset.add(LatData(lat = 33.2, lng = 45.6))

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
                    val lat = LocationService.currentLocation?.latitude
                    val long = LocationService.currentLocation?.longitude



                    if (lat != null && long != null){
                        mMap.addMarker(MarkerOptions()
                            .position(LatLng(lat, long))
                        )

                        lateinit var latData: MutableList<LatData>
                        runBlocking {
                            launch{
                                initNewLatData(latData(lat,long))
                            }
                        }
                        // myLatDataset.add(latData)

                    }

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

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}