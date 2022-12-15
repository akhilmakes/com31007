package com.example.week_5B_solution.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.exifinterface.media.ExifInterface
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.week_5B_solution.R
import com.example.week_5B_solution.model.ImageData
import com.example.week_5B_solution.viewmodel.AppViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.example.week_5B_solution.databinding.ActivityPathDetailBinding
import com.example.week_5B_solution.model.LatLngData
import com.example.week_5B_solution.model.LatLngDataDao
import com.example.week_5B_solution.model.PathDao
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class PathDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPathDetailBinding
    private var appViewModel: AppViewModel? = null

    private lateinit var dbLatLngDataDao: LatLngDataDao
    private lateinit var dbPathDao : PathDao

    private lateinit var pathLatLngList : List<LatLngData>
    private lateinit var result : List<LatLngData>
    private lateinit var pathLocation: LatLng

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    private var myDataset: MutableList<ImageData> = ArrayList<ImageData>()

    val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let{
            // https://developer.android.com/training/data-storage/shared/photopicker#persist-media-file-access
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            this@PathDetailActivity.contentResolver.takePersistableUriPermission(uri, flag)

            val input = contentResolver.openInputStream(uri)!!

            val exif = ExifInterface(input)

            var lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
            var long = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
            val longRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

            val date = exif.getAttribute(ExifInterface.TAG_DATETIME)

            if(!lat.isNullOrEmpty()) {
                lat = parseLatLng(lat)
            }
            if(!long.isNullOrEmpty()){
                long = parseLatLng(long)
            }

           val latLng = if (longRef == "W"){
                "$lat, -$long"
            } else {
                "$lat, $long"
            }


            val imageData = ImageData(
                title = "Add Title Here",
                description = "Add Description Here",
                imagePath = uri.toString(),
                pathID = pathNumber!!,
                latLng = latLng,
                dateTime = date
            )

            this.appViewModel!!.addImage(imageData, uri)
            myDataset.add(imageData)


            mRecyclerView.scrollToPosition(myDataset.size - 1)
        }
    }


    var showImageActivityResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        result?.let{
            val position = it.data?.extras?.getInt("position")
            val delete_op = it.data?.extras?.getBoolean("deletion")
            val update_op = it.data?.extras?.getBoolean("updated")
            delete_op?.apply {
                if(delete_op == true){
                    position?.apply{
                        // Tell the adapter that the collection it is rendering has changed
                        // so it can redraw itself.
                        mAdapter.notifyItemRemoved(position)
                        mAdapter.notifyItemRangeChanged(position, MyAdapter.items.size);
                        Snackbar.make(/* view = */ mRecyclerView,
                            /* text = */ "Image deleted.",
                            /* duration = */ Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }
            update_op?.apply {
                if(update_op == true){
                    Snackbar.make(/* view = */ mRecyclerView,
                        /* text = */ "Image detail updated.",
                        /* duration = */ Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun parseLatLng(exifTag: String): String{

        val degrees = exifTag.substring(0, exifTag.indexOf("/"))

        val minutes = exifTag.substring(degrees.length-1, exifTag.indexOf("/"))

        val seconds = exifTag.substring(minutes.length-1, exifTag.indexOf("/"))

        val result = degrees.toDouble() + (minutes.toDouble()/60) + (seconds.toDouble()/3600)

        return result.toString()




    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        binding = ActivityPathDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)
       
        val title = intent.getStringExtra("title")
        val pathID = intent.getIntExtra("pathID",-1)


        val cameraLatLng = this.appViewModel!!.getLastLatLng(pathID)
        pathLocation = LatLng(cameraLatLng.lat, cameraLatLng.lng)

        result = this.appViewModel!!.getAllLatLng(pathID)

        findViewById<TextView>(R.id.pathTitle).apply {
            text = title
        }

        pathNumber = pathID

        mRecyclerView = findViewById<RecyclerView>(R.id.pathImageList)

        val numberOfColumns = 4
        mRecyclerView.layoutManager = GridLayoutManager(this, numberOfColumns)

        mAdapter = MyPathAdapter(this, myDataset) as RecyclerView.Adapter<RecyclerView.ViewHolder>
        mRecyclerView.adapter = mAdapter

        initPathImages(pathNumber!!)


        val photoPickerFab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.openGalleryFab)
        photoPickerFab.setOnClickListener(View.OnClickListener { view ->
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        })

        val galleryFab: FloatingActionButton = findViewById(R.id.go_to_gallery_fab)

        galleryFab.setOnClickListener{
            val intent = Intent(applicationContext, GalleryActivity::class.java)
            startActivity(intent)
        }

//        val editPathTitleBtn: AppCompatButton = findViewById(R.id.editPathTitleBtn)

//        binding.editPathTitleBtn.setOnClickListener {
//            onUpdateButtonClickListener(it, position)
//        }



    }


    fun initPathImages(id: Int){
        val pathImages = this.appViewModel!!.retrievePathImages(id)

        myDataset.addAll(pathImages)
    }

    fun onViewHolderItemClick(position: Int) {
        val intent = Intent(this, ShowPathImageActivity::class.java)
        intent.putExtra("position", position)
        // Start the ShowImageActivity from the ActivityResultContract registered to handle
        // the result when the Activity returns
        showImageActivityResultContract.launch(intent)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true


        val polylineOptions = PolylineOptions()

        Log.d("Detail", "result is $result")

        for (i in result){
            polylineOptions.add(LatLng(i.lat, i.lng))
            Log.d("Detail", result.toString())
        }

        for (images in myDataset){

            val latLngString = images.latLng

            if(latLngString!!.substring(0, latLngString.indexOf(",")) != "null"){

                val latLongPair = getLatLongFromString(latLngString)

                mMap.addMarker(MarkerOptions().position(LatLng(latLongPair[0], latLongPair[1])))

            }



        }



        mMap.addPolyline(polylineOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathLocation, 15f))

    }

    fun getLatLongFromString(latLng: String): MutableList<Double> {

        return mutableListOf(
            latLng.substring(0, latLng.indexOf(",")).toDouble(),
            latLng.substring(latLng.indexOf(",")+1).toDouble()

        )
    }

    companion object{
        var pathNumber: Int? = null
    }

}