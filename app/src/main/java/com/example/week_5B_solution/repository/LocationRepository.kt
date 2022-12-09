package com.example.week_5B_solution.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.week_5B_solution.ImageApplication
import com.example.week_5B_solution.data.LatData
import com.example.week_5B_solution.data.LatDataDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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