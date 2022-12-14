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

    //private var pathData: List<LatLngData>
    var allLatLngList : List<LatLngData>? = null

    var markerDataList : LiveData<List<LatLngData>>? = null

    var cameraLatLng : LatLngData? = null

    var pathForId: Path? = null


    init {
        currentPath = appRepository.getPathNum()

        markerDataList = appRepository.getOneLatLngFromPath()
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

    fun getPathForID(id: Int): Path{

        viewModelScope.launch { pathForId = appRepository.getPathForID(id) }

        return pathForId!!
    }

    fun getAllLatLng(id : Int) : List<LatLngData>{
        viewModelScope.launch {
            allLatLngList = appRepository.getAllLatLng(id)
        }
        return allLatLngList!!
    }

    fun getOneLatLngFromPath() : LiveData<List<LatLngData>>{

        return markerDataList!!
    }

    fun getLatLngForCamera(id: Int) : LatLngData {
        viewModelScope.launch{
            cameraLatLng = appRepository.getLatLngForCamera(id)
        }
        return cameraLatLng!!
    }
}