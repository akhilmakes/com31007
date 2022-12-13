package com.example.week_5B_solution.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.week_5B_solution.ImageApplication
import com.example.week_5B_solution.R
import com.example.week_5B_solution.databinding.ActivityMapsBinding
import com.example.week_5B_solution.databinding.ActivityPathDetailBinding
import com.example.week_5B_solution.model.LatLngData
import com.example.week_5B_solution.model.LatLngDataDao
import com.example.week_5B_solution.model.PathDao
import com.example.week_5B_solution.viewmodel.AppViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PathDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPathDetailBinding
    private var appViewModel: AppViewModel? = null

    private lateinit var dbLatLngDataDao: LatLngDataDao
    private lateinit var dbPathDao : PathDao

    private lateinit var pathLatLngList : List<LatLngData>
    private lateinit var result : List<LatLngData>
    private lateinit var pathLocation: LatLng


    private fun initDataDao(){
        dbLatLngDataDao = (this@PathDetailActivity.application as ImageApplication)
            .databaseObj.latLngDataDao()
        dbPathDao = (this@PathDetailActivity.application as ImageApplication)
            .databaseObj.pathDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        binding = ActivityPathDetailBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_path_detail)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initDataDao()

        var markerTest = findViewById<TextView>(R.id.markerTest)
        var title = intent.getStringExtra("title")
        var pathID = intent.getIntExtra("pathID",-1)

        var cameraLatLng = this.appViewModel!!.getLatLngForCamera(pathID)
        pathLocation = LatLng(cameraLatLng.lat, cameraLatLng.lng)

        //result = initPath(pathID)
        result = this.appViewModel!!.getAllLatLng(pathID)

        markerTest.setText("$title, $pathID")

    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        // requestLocationPermissions()
        mMap = googleMap

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        var pathID = intent.getIntExtra("pathID",-1)

        val sydney = LatLng(-34.0, 151.0)

        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        // var pathLatLngList = dbLatLngDataDao.getItem(pathID)


        //Log.d("Detail", "pathID is $pathID.toString()")
        //Log.d("Detail", "camera lat value is ${cameraLatLng.lat}")
        //Log.d("Detail", "camera lng value is ${cameraLatLng.lng}")

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(cameraLatLng.lat, cameraLatLng.lng)))

        val polylineOptions = PolylineOptions()

        for (i in result){
            polylineOptions.add(LatLng(i.lat, i.lng))
            Log.d("Detail", result.toString())
        }

        mMap.addPolyline(polylineOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pathLocation))

    }

}