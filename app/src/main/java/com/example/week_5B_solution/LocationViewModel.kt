package com.example.week_5B_solution

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.week_5B_solution.data.LatData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(application: Application): AndroidViewModel(application) {

    private var locationRepo: LocationRepository = LocationRepository(application)
    private var lastLocation: LiveData<LatData>?

    init {
        this.lastLocation = this.locationRepo.retrieveLatLng()
    }

    fun getLatDataToDisplay(): LiveData<LatData> {

        if(this.lastLocation == null){
            this.lastLocation = MutableLiveData<LatData>()
        }

        Log.d("ViewModel","Location: $lastLocation")

        return this.lastLocation!!
    }

}