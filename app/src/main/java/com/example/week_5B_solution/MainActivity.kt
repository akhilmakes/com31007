package com.example.week_5B_solution


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.week_5B_solution.data.ImageData
import com.example.week_5B_solution.data.ImageDataDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
//    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private var myDataset: MutableList<ImageData> = ArrayList<ImageData>()
    private lateinit var daoObj: ImageDataDao

    //region ActivityResultContracts
    val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let{
            // https://developer.android.com/training/data-storage/shared/photopicker#persist-media-file-access
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            this@MainActivity.contentResolver.takePersistableUriPermission(uri, flag)

            lateinit var imageData: ImageData
            runBlocking {
                launch{
                    imageData = initNewImageData(uri)
                }
            }
            myDataset.add(imageData)
            mRecyclerView.scrollToPosition(myDataset.size - 1)
        }
    }

    var pickFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            val photo_uri = result.data?.extras?.getString("uri")

            photo_uri?.let{
                val uri = Uri.parse(photo_uri)

                lateinit var imageData: ImageData
                runBlocking {
                    launch{
                        imageData = initNewImageData(uri)
                    }
                }
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
        setContentView(R.layout.activity_main)

         initData()

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
        val cameraPickerFab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.openCamFab)
        cameraPickerFab.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, CameraActivity::class.java)
            pickFromCamera.launch(intent)
        })

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, MainActivity.REQUIRED_PERMISSIONS, MainActivity.REQUEST_CODE_PERMISSIONS
            )
        }
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
        if (requestCode == MainActivity.REQUEST_CODE_PERMISSIONS) {
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
//        repeat(10){
//            myDataset.add(ImageElement(R.drawable.joe1))
//            myDataset.add(ImageElement(R.drawable.joe2))
//            myDataset.add(ImageElement(R.drawable.joe3))
//        }
        daoObj = (this@MainActivity.application as ImageApplication)
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

    /**
     * Function introduced to contain the task of creating a new ImageData object
     * Image data objects are saved to DB after creation.
     */
    private suspend fun initNewImageData(uri: Uri): ImageData{

        var imageData = ImageData(
            title = "title unspecified",
            imagePath = uri.toString()
        )
        daoObj?.let {
            coroutineScope{
                val insertJob = async(Dispatchers.IO){
                    // Insert the newly created ImageData entity
                    daoObj.insert(imageData)
                }

                // The id of the newly inserted row, if successful.
                // Note that this implementation does not consider what happens if insertion fails, but really should
                val rowId = insertJob.await().toInt()

                // Using the rowId, retrieve the ImageData object from db,
                // or just update the id in the existing object
                // imageData.id = rowId
                val retrieveJob = async(Dispatchers.IO) {
                    daoObj.getItem(rowId)
                }
                imageData = retrieveJob.await()
            }
        }

        return imageData
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