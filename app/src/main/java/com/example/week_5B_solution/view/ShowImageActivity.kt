package com.example.week_5B_solution.view

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
// import android.media.ExifInterface
// import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.example.week_5B_solution.model.ImageDataDao
import com.example.week_5B_solution.databinding.ActivityShowImageBinding
import com.example.week_5B_solution.viewmodel.AppViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class ShowImageActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityShowImageBinding
    private lateinit var daoObj: ImageDataDao

    private var appViewModel: AppViewModel? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        // intent is a property of the activity. intent.extras returns any data that was pass
        // along with the intent.
        val bundle: Bundle? = intent.extras
        var position = -1

        if (bundle!= null) {
            // this is the image position in the items List
            position = bundle.getInt("position")
            if (position != -1) {
                // Display the model's data in the view. This is a lot of back and forth!
                loadImageView(position)
                binding.editTextTitle.setText(MyAdapter.items[position].title)
                MyAdapter.items[position].description?.isNotEmpty().apply {
                    binding.editTextDescription.setText(MyAdapter.items[position].description)
                }
                try {
                    val imgUri = Uri.parse(MyAdapter.items[position].imagePath)

                    val input = contentResolver.openInputStream(imgUri)!!

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

                    if(lat == long){
                        binding.editTextLatlong.setText("There is no GPS data")
                    }
                    else {
                        binding.editTextLatlong.apply {
                            if(longRef == "W") {
                                setText("$lat, -$long")
                            } else {
                                setText("$lat, $long")
                            }
                        }
                    }

                    binding.editTextDate.setText("$date")

                } catch (e: FileNotFoundException) {
                    Log.d(
                        "FileException",
                        "File Path: ${MyAdapter.items[position].imagePath} is Not Found"
                    )
                    e.printStackTrace()
                }

                // onClick listener for the update button
                binding.buttonSave.setOnClickListener {
                    onUpdateButtonClickListener(it, position)
                }

                // onClick listener for the delete button
                binding.buttonDelete.setOnClickListener {
                    onDeleteButtonClickListener(it, position)
                }

//                element.image?.let {
//                    imageView.setImageResource(it)
//                }
//                element.file_uri?.let {
//                    imageView.setImageURI(it)
//                }
            }
        }
    }



    fun parseLatLng(exifTag: String): String{

        if (exifTag.toDouble() != null){
            val degrees = exifTag.substring(0, exifTag.indexOf("/"))

            val minutes = exifTag.substring(degrees.length-1, exifTag.indexOf("/"))

            val seconds = exifTag.substring(minutes.length-1, exifTag.indexOf("/"))

            val result = degrees.toDouble() + (minutes.toDouble()/60) + (seconds.toDouble()/3600)

            return result.toString()
        }

        return exifTag


    }

    /**
     * This function will either use the file path to load the image by default
     * Or can load from MediaStore when defaultToPath is false and the
     * path host contains "com.android".
     */
    private fun loadImageView(position: Int, defaultToPath: Boolean = true){
        val image_path = MyAdapter.items[position].imagePath
        if(defaultToPath){
            loadImageViewWithPath(image_path)
        }else{
            val uri = Uri.parse(image_path)
            val host = uri.host ?: "media"
            val id = uri.lastPathSegment?.split(":")?.get(1) ?: ""

            if(host.startsWith("com.android")){
                runBlocking {
                    loadImageViewWithMediaStore(id)
                }
            }else{
                loadImageViewWithPath(image_path)
            }
        }
    }

    /**
     * function that loads images based on the image's file path only
     */
    private fun loadImageViewWithPath(path: String){
        binding.image.setImageURI(Uri.parse(path))
    }

    /**
     * function that loads images from media store. Queries for the image
     * using the id. Note, image may no longer exists in storage, or
     * might have been backed up to Google Photos, which might take a while to
     * retrieve. Retrieval will fail if there is no Internet connection.
     *
     * This is a basic media store access implementation. Media store query works
     * similar to running an SQL query
     */
    private suspend fun loadImageViewWithMediaStore(id: String){
        if(id.isEmpty() or !id.isDigitsOnly()){
            Snackbar.make(binding.image, "Unable to load image. Image not found.", Snackbar.LENGTH_LONG)
                .show()
            return
        }

        var current_access_uri: Uri? = null
        // Specify the columns that should be returned in the media store query result
        val projection = arrayOf(MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
            )

        withContext(Dispatchers.IO){
            contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Images.Media._ID + " = $id",
                null,
                null)?.use {cursor ->
                    if(cursor.moveToFirst()){
                        // Gets a current URI for the media store item
                        current_access_uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toLong())
                    }
            }
        }
        current_access_uri?.let{
            binding.image.setImageURI(current_access_uri)
        }
    }
    private fun onUpdateButtonClickListener(view: View, position: Int){
        // Update the data in the model back. This is a lot of work, back and forth!
        MyAdapter.items[position].title = binding.editTextTitle.text.toString()
        MyAdapter.items[position].description = binding.editTextDescription.text.toString()

        this.appViewModel!!.updateImage(MyAdapter.items[position])

        runBlocking {
            launch(Dispatchers.IO) {

                // Start an intent to to tell the calling activity an update happened.
                val intent = Intent(this@ShowImageActivity, GalleryActivity::class.java)
                intent.putExtra("updated",true)
                setResult(RESULT_OK,intent)
                finish()
            }
        }
    }

    private fun onDeleteButtonClickListener(view: View, position: Int){

        // No confirmation request from user. You should do this in practice

        this.appViewModel!!.deleteImage(MyAdapter.items[position])


        runBlocking {
            launch(Dispatchers.IO) {
                // Start intent and include data to let the calling activity know a deletion happened (include position payload
                val cacheFile = File(this@ShowImageActivity.cacheDir, MyAdapter.items[position].thumbnail)
                cacheFile.delete()
                MyAdapter.items.removeAt(position)

                // Start an intent to let the caller know deletion happened and which item was deleted.
                val intent = Intent(this@ShowImageActivity, GalleryActivity::class.java)
                intent.putExtra("deletion",true)
                intent.putExtra("position",position)
                setResult(RESULT_OK,intent)
                finish()
            }
        }
    }
    @SuppressLint("Range")
    fun uri2path(context: Context, contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(contentUri, proj, null, null, null)
        cursor?.moveToNext()
        val path = cursor?.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
        val uri = Uri.fromFile(File(path))
        cursor?.close()
        return path
    }


}