package com.example.com31007_assignment.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.com31007_assignment.model.ImageData
import com.example.com31007_assignment.model.LatLngData
import com.example.com31007_assignment.model.LocationTitle
import com.example.com31007_assignment.model.Path
import com.example.com31007_assignment.repository.AppRepository
import kotlinx.coroutines.launch

class AppViewModel(application: Application): AndroidViewModel(application) {

    private var appRepository: AppRepository = AppRepository(application)

    private var currentPath: LiveData<Int> = appRepository.getPathNum()

    private var pathImageList: List<ImageData>? = null

    private var pathInfo: Path? = null

    var allLatLngList : List<LatLngData>? = null

    var markerDataList : List<LocationTitle>? = null

    var cameraLatLng : LatLngData? = null

    var airPressure: Float? = null


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

    fun getLatestPressure(): Float?{

        viewModelScope.launch {
            airPressure = appRepository.getPressure()
        }

        return airPressure


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


    fun deletePath(id:Int){
        viewModelScope.launch {
            appRepository.deletePath(id)
        }
    }

    fun sortByPathID(): List<ImageData>{
        viewModelScope.launch {
            pathImageList = appRepository.sortByPath()
        }

        return pathImageList!!
    }
}