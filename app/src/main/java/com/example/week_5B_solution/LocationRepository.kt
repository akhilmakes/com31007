package com.example.week_5B_solution

import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.week_5B_solution.data.AppDatabase
import com.example.week_5B_solution.data.LatData
import com.example.week_5B_solution.data.LatDataDao
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.net.ssl.ManagerFactoryParameters

class LocationRepository(application: Application) {
    private var dbLatDataDao: LatDataDao? = null

    init {
        dbLatDataDao = (application as ImageApplication)
            .databaseObj.latDataDao()

    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
        private class InsertAsyncTask(private val dao: LatDataDao?) : ViewModel() {
            suspend fun insertInBackground(vararg params: LatData) {
                scope.launch {
                    for(param in params){
                        val insertedLatData = this@InsertAsyncTask.dao?.insert(param)
                        // you may want to check if insertedId is null to confirm successful insertion
                        //Log.i("MyRepository", "number generated: " + param.number.toString()
                        //        + ", inserted with id: " + insertedId.toString() + "")
                    }
                }
            }
        }
    }

    fun retrieveLatLng(): LiveData<LatData>? {

        return dbLatDataDao!!.getLatLng()
    }


}