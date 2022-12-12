package com.example.week_5B_solution.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.week_5B_solution.model.ImageData
import com.example.week_5B_solution.repository.AppRepository
import kotlinx.coroutines.launch

class AppViewModel(application: Application): AndroidViewModel(application) {

    private var appRepository: AppRepository = AppRepository(application)

    private var currentPath: LiveData<Int>


    init {
         currentPath = appRepository.getPathNum()
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


    fun generateNewPath(){
        viewModelScope.launch { appRepository.generateNewPath() }
    }

    fun retrieveCurrentPath(): LiveData<Int>{

        return this.currentPath
    }



}