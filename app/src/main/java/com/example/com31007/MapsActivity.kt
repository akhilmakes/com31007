package com.example.com31007

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

import pl.aprilapps.easyphotopicker.*
import java.text.DateFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var easyImage: EasyImage
    private lateinit var ctx: Context

    private lateinit var locationRequest: LocationRequest
    private var mCurrentLocation: Location? = null
    private var mLastUpdateTime: String? = null
    private var mLocationPendingIntent: PendingIntent? = null
    private val ACCESS_FINE_LOCATION = 123
    private var mButtonStart: Button? = null
    private var mButtonEnd: Button? = null


    private lateinit var fusedLocationClient : FusedLocationProviderClient



    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivity(this)
        setContext(this)


        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEasyImage()

        var actionBar = getSupportActionBar()

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mButtonStart = findViewById<View>(R.id.button_start) as Button
        mButtonStart!!.setOnClickListener {
            startLocationUpdates()
            if (mButtonEnd != null) mButtonEnd!!.isEnabled = true
            mButtonStart!!.isEnabled = false
        }
        mButtonStart!!.isEnabled = true

        mButtonEnd = findViewById<View>(R.id.button_end) as Button
        mButtonEnd!!.setOnClickListener {
            stopLocationUpdates()
            if (mButtonStart != null) mButtonStart!!.isEnabled = true
            mButtonEnd!!.isEnabled = false
        }
        mButtonEnd!!.isEnabled = false

        // Floating Action Button to select an image from the gallery
        val fabNewImage: FloatingActionButton = findViewById(R.id.fab_new_image)
        fabNewImage.setOnClickListener(){
            easyImage.openChooser(this@MapsActivity)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }


    @SuppressLint("MissingPermission")
    private fun checkpermission(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESS_FINE_LOCATION
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return
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

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.back_action ->{
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
        }
        return super.onContextItemSelected(item)
    }





    private fun initEasyImage() {
        easyImage = EasyImage.Builder(this)
            .setChooserTitle("Pick media")
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .allowMultiple(true)
            .setCopyImagesToPublicGalleryFolder(true)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        Log.e("Location update", "Starting...")
        //  start receiving the location update

        val intent = Intent(ctx, LocationService::class.java)
        mLocationPendingIntent =
            PendingIntent.getService(ctx,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        Log.e("IntentService", "Getting...")

//        Or call the startService() inside the lambda
//        this was an implicit invocation in the code above
//        Intent(ctx, LocationService::class.java).also { intent ->
//            startService(intent)
//            mLocationPendingIntent =
//                PendingIntent.getService(ctx,
//                    1,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//                )
//        }

        val locationTask = fusedLocationClient.requestLocationUpdates(
            locationRequest,
            mLocationPendingIntent!!
        )
        locationTask.addOnFailureListener { e ->
            if (e is ApiException) {
                e.message?.let { Log.w("MapsActivity", it) }
            } else {
                Log.w("MapsActivity", e.message!!)
            }
        }
        locationTask.addOnCompleteListener {
            Log.d(
                "MapsActivity",
                "starting gps successful!"
            )
        }

    }
    private fun stopLocationUpdates() {
        //  stop receiving the location update
        Log.e("Location", "update stop")
        fusedLocationClient.removeLocationUpdates(mLocationPendingIntent!!)
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            mCurrentLocation = locationResult.lastLocation
            mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
//            Log.i("MAP", "new location " + mCurrentLocation.toString())
            mMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        mCurrentLocation!!.latitude,
                        mCurrentLocation!!.longitude
                    )
                )
                    .title(mLastUpdateTime)
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        mCurrentLocation!!.latitude,
                        mCurrentLocation!!.longitude
                    ), 14.0f
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkpermission()
        startLocationUpdates()
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


        val polyline1 = mMap.addPolyline(
            PolylineOptions()
            .clickable(true)
            .add(lastPos)) /* Not sure how this part is meant to work with poly-lines */
        mMap.addMarker(MarkerOptions().position(lastPos).title("Last Position"))

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos,10.0f))



    }

    private fun setContext(context: Context) {
        ctx = context
    }


    companion object{
        private var activity: AppCompatActivity? = null
        private lateinit var mMap: GoogleMap

        fun getActivity(): AppCompatActivity? {
            return activity

        }
        fun setActivity(newActivity: AppCompatActivity) {
            activity = newActivity
        }
        fun getMap(): GoogleMap {
            return mMap
        }

        private const val  PERMISSION_REQUEST_CODE = 1
    }


}