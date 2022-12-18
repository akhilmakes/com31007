package com.example.com31007_assignment.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.com31007_assignment.ImageApplication
import com.example.com31007_assignment.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AppRepository(application: Application) {
    private var dbPathDao: PathDao? = null
    private var dbLatLngDataDao: LatLngDataDao? = null
    private var dbImageDataDao: ImageDataDao? = null

    private lateinit var pathLatLngList : List<LatLngData>
    private lateinit var latLngForMarkerList : List<LocationTitle>
    private lateinit var latLngForCamera : LatLngData
    private lateinit var pathForID: Path
    private lateinit var pathTitle: String

    private var pathImages: List<ImageData>? = null

    private var airPressure: Float? = null

    init {

        dbPathDao = (application as ImageApplication)
            .databaseObj.pathDao()
        dbLatLngDataDao = (application)
            .databaseObj.latLngDataDao()
        dbImageDataDao = (application)
            .databaseObj.imageDataDao()

    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
        private class InsertAsyncTaskPath(private val dao: PathDao?) : ViewModel() {
            suspend fun insertInBackground(vararg params: Path) {
                scope.launch {
                    for(param in params){
                        this@InsertAsyncTaskPath.dao?.insert(param)
                    }
                }
            }
        }

        private class InsertAsyncTaskImageData(private val dao: ImageDataDao?) : ViewModel() {
            suspend fun insertInBackground(vararg params: ImageData) {
                scope.launch {
                    for(param in params){
                       this@InsertAsyncTaskImageData.dao?.insert(param)
                    }
                }
            }
        }

        private class UpdateAsyncTaskImageData(private val dao: ImageDataDao?) : ViewModel() {
            suspend fun updateInBackground(vararg params: ImageData) {
                scope.launch {
                    for(param in params){
                        this@UpdateAsyncTaskImageData.dao?.update(param)
                    }
                }
            }
        }

        private class DeleteAsyncTaskImageData(private val dao: ImageDataDao?) : ViewModel() {
            suspend fun deleteInBackground(vararg params: ImageData) {
                scope.launch {
                    for(param in params){
                        this@DeleteAsyncTaskImageData.dao?.delete(param)
                    }
                }
            }
        }
    }
    //region Functions to Access/Update LatLngData Table

    fun getAllLatLng(id : Int) : List<LatLngData>{
        runBlocking {
            launch(Dispatchers.Default){
                pathLatLngList = dbLatLngDataDao!!.getItem(id)
            }

        }

        return pathLatLngList
    }

    fun getPressure(): Float?{


        runBlocking {
            launch (Dispatchers.IO){

                airPressure = dbLatLngDataDao!!.getLatestPressureReading()

            }
        }

        return airPressure
    }

    fun getOneLatLngFromPath() : List<LocationTitle>{
        runBlocking {
            launch(Dispatchers.Default) {
                latLngForMarkerList = dbLatLngDataDao!!.getOneLatLngFromPath()
            }
        }
        return latLngForMarkerList
    }

    fun getLastLatLng(id : Int) : LatLngData {
        runBlocking {
            launch(Dispatchers.Default) {
                latLngForCamera = dbLatLngDataDao!!.getLatLng(id)
            }
        }
        return latLngForCamera
    }

    //endregion Functions to Access/Update LatLngData Table

    //region Functions to Access/Update ImageData Table

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

    fun sortByPath() :List<ImageData>{
        runBlocking {
            launch(Dispatchers.Default){
                pathImages =  dbImageDataDao!!.sortByPath()
            }
        }
        return pathImages!!
    }

    //endregion Functions to Access/Update ImageData Table

    //region Functions to Access/Update Path Table

    fun getPathNum(): LiveData<Int> {
        return dbPathDao!!.getLatestPathNum()
    }

    suspend fun generateNewPath(){
        InsertAsyncTaskPath(dbPathDao).insertInBackground(Path(title = "Add title here"))
    }

    fun getPathForID(id : Int): Path{
        runBlocking {
            launch (Dispatchers.Default){
                pathForID = dbPathDao!!.getPath(id)
            }
        }
        return pathForID
    }

    fun updatePathTitle(title: String, id : Int) {
        runBlocking {
            launch(Dispatchers.Default){
                dbPathDao!!.updateTitle(title, id)
            }
        }
    }

    fun getTitle(id:Int): String{
        runBlocking {
            launch(Dispatchers.Default){
                pathTitle =  dbPathDao!!.getTitle(id)
            }
        }

        return pathTitle
    }

    fun deletePath(id:Int){
        runBlocking {
            launch(Dispatchers.Default){
                dbPathDao!!.deletePath(id)
            }
        }
    }

    //endregion Functions to Access/Update Path Table


}