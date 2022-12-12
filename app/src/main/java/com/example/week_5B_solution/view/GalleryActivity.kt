package com.example.week_5B_solution.view


import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.week_5B_solution.ImageApplication
import com.example.week_5B_solution.R
import com.example.week_5B_solution.model.CameraActivity
import com.example.week_5B_solution.model.ImageData
import com.example.week_5B_solution.model.ImageDataDao
import com.example.week_5B_solution.model.LocationService
import com.example.week_5B_solution.viewmodel.AppViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class GalleryActivity : AppCompatActivity() {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
//    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private var myDataset: MutableList<ImageData> = ArrayList<ImageData>()
    private lateinit var daoObj: ImageDataDao

    private var pathNumber: Int? = null

    private var appViewModel: AppViewModel? = null

    //region ActivityResultContracts
    val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let{
            // https://developer.android.com/training/data-storage/shared/photopicker#persist-media-file-access
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            this@GalleryActivity.contentResolver.takePersistableUriPermission(uri, flag)

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

    var pickFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            val photo_uri = result.data?.extras?.getString("uri")

            photo_uri?.let{
                val uri = Uri.parse(photo_uri)

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

    var showImageActivityResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
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

    //endregion ActivityResultContracts

    //region overriden functions from FragmentActivity super class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        initData()

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        this.appViewModel!!.retrieveCurrentPath().observe(this, Observer {
            currentPath ->

            pathNumber = currentPath
        })

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        mRecyclerView = findViewById<RecyclerView>(R.id.my_list)

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        val numberOfColumns = 4
        mRecyclerView.layoutManager = GridLayoutManager(this, numberOfColumns)

        // specify an adapter (see also next example)
        mAdapter = MyAdapter(this, myDataset) as RecyclerView.Adapter<RecyclerView.ViewHolder>
        mRecyclerView.adapter = mAdapter

        // Start a photo picker Activity from the ActivityResultContract registered to handle
        // the result when the Activity returns
        val photoPickerFab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.openGalleryFab)
        photoPickerFab.setOnClickListener(View.OnClickListener { view ->
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        })

        // Start the CameraActivity using the ActivityResultContract registered to handle
        // the result when the Activity returns
        val cameraPickerFab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.openCamFab).apply {
            if (isLocationServiceRunning()) show()
            else hide()
        }
        cameraPickerFab.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, CameraActivity::class.java)
            pickFromCamera.launch(intent)
        })




        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
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



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    // called to request permissions
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Toast.makeText(this,
                    "All permissions granted by the user.",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    "Not all permissions granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    //endregion overriden functions from FragmentActivity super class

    //region other (custom/utility) functions

    private fun initData() {
        daoObj = (this@GalleryActivity.application as ImageApplication)
            .databaseObj.imageDataDao()
        runBlocking {
            launch(Dispatchers.Default) {
                myDataset.addAll(daoObj.getItems())
            }
        }
    }

    /**
     * onClick listener for the Adapter's ViewHolder item click
     */
    fun onViewHolderItemClick(position: Int) {
        val intent = Intent(this, ShowImageActivity::class.java)
        intent.putExtra("position", position)
        // Start the ShowImageActivity from the ActivityResultContract registered to handle
        // the result when the Activity returns
        showImageActivityResultContract.launch(intent)
    }


    // Called in onCreate to check if permissions have been granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    //endregion other (custom/utility) functions

    companion object {
        const val TAG = R.string.app_name.toString()
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10
        const val REQUEST_CODE_LOCATION_PERMISSION = 1

        const val GRANTED = PackageManager.PERMISSION_GRANTED

        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(Manifest.permission.ACCESS_MEDIA_LOCATION)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }.toTypedArray()
    }
}