package com.example.com31007

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.com31007.databinding.ActivityMapsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

import pl.aprilapps.easyphotopicker.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener, OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var easyImage: EasyImage


    private fun hasLocationsPermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun hasWriteToStoragePermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun hasReadFromStoragePermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)


    private fun checkAndRequestPermissions() {

        var permissionsToRequest  = mutableListOf<String>()

        if(hasLocationsPermissions() != PackageManager.PERMISSION_GRANTED){
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(hasWriteToStoragePermissions() != PackageManager.PERMISSION_GRANTED){
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(hasReadFromStoragePermissions() != PackageManager.PERMISSION_GRANTED){
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }


        if(permissionsToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
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

    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)


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