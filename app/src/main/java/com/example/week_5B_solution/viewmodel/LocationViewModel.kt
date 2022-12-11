package com.example.week_5B_solution.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.week_5B_solution.repository.LocationRepository
import com.example.week_5B_solution.data.LatLngData
import kotlinx.coroutines.launch

class LocationViewModel(application: Application): AndroidViewModel(application) {

    private var locationRepo: LocationRepository = LocationRepository(application)
    private var locations: LiveData<List<LatLngData>>?


    init {
        this.locations = this.locationRepo.retrieveLatLngList()
    }

    fun getLatDataToDisplay(): LiveData<List<LatLngData>> {
        val lastLocation = this.locations!!.value!!.last()

        if(this.locations == null){
            this.locations = MutableLiveData()
        }

        Log.d("ViewModel","Location: ${lastLocation.lat}, ${lastLocation.lng}")

        return this.locations!!
    }


    fun generateNewPath(){
        viewModelScope.launch { locationRepo.generateNewPath() }
    }

}