package com.example.week_5B_solution

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.week_5B_solution.data.ImageData
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private lateinit var context: Context


    //region constructors

    constructor(items: List<ImageData>) {
        MyAdapter.items = items as MutableList<ImageData>
    }

    constructor(cont: Context, items: List<ImageData>) : super() {
        MyAdapter.items = items as MutableList<ImageData>
        context = cont
    }

    //endregion constructors

    //region overriden functions from RecyclerView.Adapyer super class

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate the layout, initialize the View Holder
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_image,
            parent, false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // region collapsible comment region

        //Use the provided View Holder on the onCreateViewHolder method to populate the
        // current row on the RecyclerView

        // Images added from drawable resources
//        items[position].image?.let {
//            holder.imageView.setImageResource(it)
//        }


        // Images loaded into the view
//        items[position].file_uri?.let {
//            holder.imageView.setImageURI(it)
//        }

        /**
         * Thumbnail of images loaded into the view
         */
//        items[position].file_uri?.let{
//             runBlocking() create a scope within the main thread - allowing safe access to Contacts.Intents.UI elements
//            var file_uri = it  // the implicit "it" seems to be causing confusion

        //************************************************************************
        //          Code above is from the previous labs.
        //          Commented out only to help support learning.
        //************************************************************************

        //endregion collapsed comment region

        // Load thumbnail from cache if it exists.
        // Thumbnails in cache may be cleared at any time,
        // so don't assume it is always there, even after creating one.
        if(items[position].thumbnail != null){
            var thumbnailCacheFile = File(context.cacheDir, items[position].thumbnail)

            if(thumbnailCacheFile.exists()){
                holder.imageView.setImageURI(thumbnailCacheFile.toUri())
                 Log.d("Lab5B", "Thumbnail ${items[position].thumbnail} found and loaded")
            }else{
                items[position].thumbnail = null
                Log.d("Lab5B", "Thumbnail ${items[position].thumbnail} NOT found, will create")
            }
        }

        // When thumbnail is not in cache, create one
        if(items[position].thumbnail == null){
            var thumbnailCacheFile: File? = null
            Log.d("Lab5B", "Thumbnail ${items[position].thumbnail} NOT found, creating...")
            // Create the thumbnail, save it and load it
            runBlocking{
                val imageFileUri = Uri.parse(items[position].imagePath)
                // launch() will start a child coroutine that uses the default dispatcher,
                // which will run outside the main thread.
                launch(Dispatchers.IO){
                    imageFileUri.path?.let { it ->
                        var myBitmap: Bitmap? = makeThumbnail(imageFileUri, true)

                        thumbnailCacheFile = myBitmap?.let {
                            Log.d("Lab5B", "Thumbnail bitmap created.")
                             cacheThumbnailBitmap(it)
                        }

                        // If a thumbnail was created, update the ImageData object
                        thumbnailCacheFile?.let {
                            Log.d("Lab5B", "Thumbnail file, with filename: ${it.name}. Exists?: ${it.exists()}")
                            // Saving the ImageData to DB is implemented here, but should it?
                            // Think how changing the architecture might solve this.
                            items[position].thumbnail = it.name
                            var daoObj = (context.applicationContext as ImageApplication)
                                .databaseObj.imageDataDao()
                            daoObj.update(items[position])
                        }

                    }
                }
                // Code used to list Cache files in log
                // context.cacheDir.listFiles()?.forEach {
                //    Log.d("Lab5B", "Listing cache items: ${it.name}") }
            }
            thumbnailCacheFile?.let {
                Log.d("Lab5B", "Got a thumbnail file object back in main thread and it exists: ${it.exists()}")
                holder.imageView.setImageURI(it.toUri())
            }

        }

        // onClick listener added to each item in the ViewHolder
        holder.itemView.setOnClickListener(View.OnClickListener {
            // the listener is implemented in MainActivity
            (context as MainActivity).onViewHolderItemClick(position)
        })
    }

    override fun getItemCount(): Int {
        return MyAdapter.items.size
    }

    //endregion overriden functions from RecyclerView.Adapyer super class

    //region other (custom/utility) functions

    /**
     * All functions that could block the main thread should be marked as suspending functions.
     * This forces the consumer to dispatch them to a background thread.
     *
     * This function makes a thumbnail as a bitmap - created to make it easy to choose
     * between calling decodeSampledBitmapFromResource() and contentResolver.loadThumbnail()
     */
    private suspend fun makeThumbnail(uri: Uri, useloadThumbnail: Boolean = false): Bitmap?{
        return if (useloadThumbnail && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // of course useloadThumbnail is a better option, but decodeSampledBitmapFromResource allows you to
            // better appreciate the impact of long running process.
            // Also, useloadThumbnail is only available with API 29 or greater
            context.contentResolver.loadThumbnail(uri, Size(250, 250), null)
        } else {
            // even if useThumbnailFunc is true but the API is older than 29,
            // fallback to decodeSampledBitmapFromResource
            MyAdapter.decodeSampledBitmapFromResource(uri, 150, 150, context.contentResolver)
        }
    }

    /**
     * All functions that could block the main thread should be marked as suspending functions.
     * This forces the consumer to dispatch them to a background thread.
     *
     * Function takes a bitmap (thumbnail in this use case) and saves the bitmap to
     * cache as a jpeg file. Returns a File object pointing to the image file
     */
    private suspend fun cacheThumbnailBitmap(bitmap: Bitmap): File?{
        var thumbnailCacheFile: File? = null

        withContext(Dispatchers.IO){
            bitmap.let{
                //Convert bitmap to byte array
                val bos = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 0, bos)
                val bitmapByteArray = bos.toByteArray()

                // Save the byte array to file in the file cache directory
                // ideally you want to check cache quota before doing this.
                thumbnailCacheFile = File.createTempFile("lab5B_", ".jpg", context.cacheDir)
                val fos = FileOutputStream(thumbnailCacheFile)
                fos.write(bitmapByteArray)
                fos.flush()
                fos.close()
            }
        }
        return thumbnailCacheFile
    }

    //endregion other (custom/utility) functions

    companion object {
        var items: MutableList<ImageData> = ArrayList<ImageData>()

        /**
         * All functions that could block the main thread should be marked as suspending functions.
         * This forces the consumer to dispatch them to a background thread.
         */
        private suspend fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val height = options.outHeight; val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = (height / 2).toInt()
                val halfWidth = (width / 2).toInt()

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize.toInt();
        }

        /**
         * All functions that could block the main thread should be marked as suspending functions.
         * This forces the consumer to dispatch them to a background thread.
         *
         * Note the following changes from the sample you were originally provided:
         * signature now uses uri: Uri instread of filePath: String, and added resolver: ContentResolver
         * This is due to an error you might have observed - BitmapFactory.decodeFile failed to load the
         * image file. My current guess (yet to confirm) is that that library needs an update (since the app itself has
         * access permission to the file.
         *
         * Consequent of this, had to implement a workaround using ImageDecoder.decodeBitmap - which is able to
         * access the file and create an Bitmap object from it. It also nicely offers an option to reduce image quality
         * before the rest of the function resizes it into a thumbnail - reduced to 20% of the original quality here
         */
        suspend fun decodeSampledBitmapFromResource(uri: Uri, reqWidth: Int, reqHeight: Int, resolver: ContentResolver): Bitmap {
            return BitmapFactory.Options().run {

                var inmemoryBitmap: Bitmap? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri))
                    } else {
                        resolver.openInputStream(uri)?.use{
                            BitmapFactory.decodeStream(it)
                        }
                    }

                var byteArraySteam = ByteArrayOutputStream()
                var byteArray = inmemoryBitmap?.let{
                    it.compress(Bitmap.CompressFormat.JPEG, 0, byteArraySteam) // Quality 0 means no change
                    byteArraySteam.toByteArray()
                }

                // First decode with inJustDecodeBounds=true to check dimensions
                inJustDecodeBounds = true
                byteArray?.let {
                    BitmapFactory.decodeByteArray(byteArray, 0, it.size, this) }
                // BitmapFactory.decodeFile(filePath, this) - old code before refactoring

                // Calculate inSampleSize
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false
                return byteArray?.let {
                    BitmapFactory.decodeByteArray(byteArray, 0, it.size, this) }!!
                // note the use of kotlin null assert !! - https://kotlinlang.org/docs/null-safety.html#the-operator
            }
        }
    }

    //region Internal classes

    public class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        lateinit var image: Image
//        var title: TextView = itemView.findViewById<View>(R.id.title) as TextView
//        var preview: TextView = itemView.findViewById<View>(R.id.preview) as TextView
        var imageView: ImageView = itemView.findViewById<View>(R.id.image_item) as ImageView

    }

    //endregion Internal classes
}