package com.example.week_5B_solution.viewmodel

import android.app.Application
import android.media.Image
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.week_5B_solution.model.ImageData
import com.example.week_5B_solution.model.LatLngData
import com.example.week_5B_solution.model.LocationTitle
import com.example.week_5B_solution.model.Path
import com.example.week_5B_solution.repository.AppRepository
import kotlinx.coroutines.launch

class AppViewModel(application: Application): AndroidViewModel(application) {

    private var appRepository: AppRepository = AppRepository(application)

    private var currentPath: LiveData<Int>

    private var pathImageList: List<ImageData>? = null

    private var pathInfo: Path? = null

    //private var pathData: List<LatLngData>
    var allLatLngList : List<LatLngData>? = null

    var markerDataList : List<LocationTitle>? = null

    var cameraLatLng : LatLngData? = null



    init {
        currentPath = appRepository.getPathNum()
        // pathData = appRepository.getAllLatLng()
    }


    fun addImage(imageData: ImageData, uri: Uri): ImageData {

        viewModelScope.launch { appRepository.insertImage(imageData) }

        return imageData

    }

    fun updateImage(imageData: ImageData){

        viewModelScope.launch { appRepository.updateImage(imageData) }

    }

    fun deleteImage(imageData: ImageData){

        viewModelScope.launch { appRepository.deleteImage(imageData) }

    }

    fun retrievePathImages(pathID: Int): List<ImageData>{

        viewModelScope.launch {  pathImageList = appRepository.getPathImages(pathID) }

        return pathImageList!!
    }


    fun generateNewPath(){
        viewModelScope.launch { appRepository.generateNewPath() }
    }

    fun retrieveCurrentPath(): LiveData<Int>{

        return this.currentPath
    }

    fun getPathForID(id:Int): Path{

        viewModelScope.launch { pathInfo = appRepository.getPathForID(id) }

        return pathInfo!!

    }

    fun getAllLatLng(id : Int) : List<LatLngData>{
        viewModelScope.launch {
            allLatLngList = appRepository.getAllLatLng(id)
        }
        return allLatLngList!!
    }

    fun getOneLatLngFromPath() : List<LocationTitle>{
        viewModelScope.launch{
            markerDataList = appRepository.getOneLatLngFromPath()
        }
        return markerDataList!!
    }

    fun getLastLatLng(id: Int) : LatLngData {
        viewModelScope.launch{
            cameraLatLng = appRepository.getLastLatLng(id)
        }
        return cameraLatLng!!
    }

    fun updatePathTitle(title:String, id:Int){
        viewModelScope.launch {
            appRepository.updatePathTitle(title, id)
        }
    }

    fun getTitle(id:Int): String{
        lateinit var pathTitle: String

        viewModelScope.launch {
            pathTitle = appRepository.getTitle(id)
        }

        return pathTitle
    }

    fun deletePath(id:Int){
        viewModelScope.launch {
            appRepository.deletePath(id)
        }
    }

    fun searchImage(search:String){
        viewModelScope.launch {
            appRepository.searchImage(search)
        }
    }
}