package com.example.com31007

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.com31007.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

import pl.aprilapps.easyphotopicker.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener, OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var easyImage: EasyImage

    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private var requestingLocationUpdates: Boolean = true
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest


    private fun hasLocationsPermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun hasBackgroundLocationPermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    private fun hasWriteToStoragePermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun hasReadFromStoragePermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)




    private fun requestStoragePermissions(){
        var storagePermissions = mutableListOf<String>()

        if(hasWriteToStoragePermissions() != PackageManager.PERMISSION_GRANTED){
            storagePermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(hasReadFromStoragePermissions() != PackageManager.PERMISSION_GRANTED){
            storagePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(storagePermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(this, storagePermissions.toTypedArray(), 0)
        }
    }

    private fun requestLocationPermissions() {

        var locationPermissions  = mutableListOf<String>()

        if(hasLocationsPermissions() != PackageManager.PERMISSION_GRANTED){
            locationPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(hasBackgroundLocationPermissions() != PackageManager.PERMISSION_GRANTED){
            locationPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if(locationPermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(this, locationPermissions.toTypedArray(), 1)
        }

    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0 && grantResults.isNotEmpty()){
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission Request:", "${permissions[i]} granted")
                }

            }
        }

        if (requestCode == 1 && grantResults.isNotEmpty()){
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission Request:", "${permissions[i]} granted")
                }

            }
        }

    }

    private fun checkAndRequestPermissions(){
        requestStoragePermissions()
        requestLocationPermissions()
    }



    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
            }
        }



        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEasyImage()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Floating Action Button to select an image from the gallery
        val fabNewImage: FloatingActionButton = findViewById(R.id.fab_new_image)
        fabNewImage.setOnClickListener(){
            easyImage.openChooser(this)
        }

    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }


    private fun initEasyImage() {
        easyImage = EasyImage.Builder(this)
            .setChooserTitle("Pick media")
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .allowMultiple(true)
            .setCopyImagesToPublicGalleryFolder(true)
            .build()
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
        var lastPos = LatLng(0.0,0.0)


        mMap = googleMap

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)


        mMap.addMarker(MarkerOptions().position(lastPos).title("Last Position"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos,10.0f))



    }



    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }


    companion object{
        private const val  PERMISSION_REQUEST_CODE = 1
    }


}