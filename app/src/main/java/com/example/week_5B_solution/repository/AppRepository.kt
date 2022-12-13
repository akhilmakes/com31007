package com.example.week_5B_solution.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.week_5B_solution.ImageApplication
import com.example.week_5B_solution.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AppRepository(application: Application) {
    private var dbPathDao: PathDao? = null
    private var dbLatLngDataDao: LatLngDataDao? = null
    private var dbImageDataDao: ImageDataDao? = null

    private var pathImages: List<ImageData>? = null

    init {
        dbPathDao = (application as ImageApplication)
            .databaseObj.pathDao()
        dbLatLngDataDao = (application as ImageApplication)
            .databaseObj.latLngDataDao()
        dbImageDataDao = (application as ImageApplication)
            .databaseObj.imageDataDao()

    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
        private class InsertAsyncTaskPath(private val dao: PathDao?) : ViewModel() {
            suspend fun insertInBackground(vararg params: Path) {
                scope.launch {
                    for(param in params){
                        val insertedLatData = this@InsertAsyncTaskPath.dao?.insert(param)
                        // you may want to check if insertedId is null to confirm successful insertion
                        //Log.i("MyRepository", "number generated: " + param.number.toString()
                        //        + ", inserted with id: " + insertedId.toString() + "")
                    }
                }
            }
        }

        private class InsertAsyncTaskImageData(private val dao: ImageDataDao?) : ViewModel() {
            suspend fun insertInBackground(vararg params: ImageData) {
                scope.launch {
                    for(param in params){
                        val insertedLatData = this@InsertAsyncTaskImageData.dao?.insert(param)
                        // you may want to check if insertedId is null to confirm successful insertion
                        //Log.i("MyRepository", "number generated: " + param.number.toString()
                        //        + ", inserted with id: " + insertedId.toString() + "")
                    }
                }
            }
        }

        private class UpdateAsyncTaskImageData(private val dao: ImageDataDao?) : ViewModel() {
            suspend fun updateInBackground(vararg params: ImageData) {
                scope.launch {
                    for(param in params){
                        val insertedLatData = this@UpdateAsyncTaskImageData.dao?.update(param)
                        // you may want to check if insertedId is null to confirm successful insertion
                        //Log.i("MyRepository", "number generated: " + param.number.toString()
                        //        + ", inserted with id: " + insertedId.toString() + "")
                    }
                }
            }
        }

        private class DeleteAsyncTaskImageData(private val dao: ImageDataDao?) : ViewModel() {
            suspend fun deleteInBackground(vararg params: ImageData) {
                scope.launch {
                    for(param in params){
                        val insertedLatData = this@DeleteAsyncTaskImageData.dao?.delete(param)
                        // you may want to check if insertedId is null to confirm successful insertion
                        //Log.i("MyRepository", "number generated: " + param.number.toString()
                        //        + ", inserted with id: " + insertedId.toString() + "")
                    }
                }
            }
        }
    }
    //Functions to Access/Update LatLngData Table

    //Functions to Access/Update ImageData Table

    suspend fun insertImage(imageData: ImageData){

        InsertAsyncTaskImageData(dbImageDataDao).insertInBackground(imageData)

    }

    suspend fun updateImage(imageData: ImageData){

        UpdateAsyncTaskImageData(dbImageDataDao).updateInBackground(imageData)

    }

    suspend fun deleteImage(imageData: ImageData){

        DeleteAsyncTaskImageData(dbImageDataDao).deleteInBackground(imageData)

    }

    suspend fun getPathImages(pathId: Int): List<ImageData>{



        runBlocking {
            launch(Dispatchers.Default){

               pathImages = dbImageDataDao!!.getAllPathImages(pathId)

            }
        }

        return pathImages!!

    }



    //Functions to Access/Update Path Table

    fun getPathNum(): LiveData<Int> {
        return dbPathDao!!.getLatestPathNum()
    }

    suspend fun generateNewPath(){
        InsertAsyncTaskPath(dbPathDao).insertInBackground(Path(title = "Add title here"))
    }

}