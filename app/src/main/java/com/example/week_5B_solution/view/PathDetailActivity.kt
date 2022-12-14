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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.week_5B_solution.ImageApplication
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

            val imageData = ImageData(
                title = "Add Title Here",
                description = "Add Description Here",
                imagePath = uri.toString(),
                pathID = pathNumber!!
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


        val cameraLatLng = this.appViewModel!!.getLatLngForCamera(pathID)
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

        // var pathLatLngList = dbLatLngDataDao.getItem(pathID)

        //Log.d("Detail", "pathID is $pathID.toString()")
        //Log.d("Detail", "camera lat value is ${cameraLatLng.lat}")
        //Log.d("Detail", "camera lng value is ${cameraLatLng.lng}")

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(cameraLatLng.lat, cameraLatLng.lng)))

        val polylineOptions = PolylineOptions()

        Log.d("Detail", "result is $result")

        for (i in result){
            polylineOptions.add(LatLng(i.lat, i.lng))
            Log.d("Detail", result.toString())
        }

        mMap.addPolyline(polylineOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathLocation, 15f))

    }

    companion object{
        var pathNumber: Int? = null
    }

}