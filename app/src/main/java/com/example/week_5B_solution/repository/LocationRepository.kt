package com.example.week_5B_solution.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.week_5B_solution.ImageApplication
import com.example.week_5B_solution.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationRepository(application: Application) {
    private var dbPathDao: PathDao? = null
    private var dbLatLngDataDao: LatLngDataDao? = null

    init {
        dbPathDao = (application as ImageApplication)
            .databaseObj.pathDao()
        dbLatLngDataDao = (application as ImageApplication)
            .databaseObj.latLngDataDao()

    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
        private class InsertAsyncTask(private val dao: PathDao?) : ViewModel() {
            suspend fun insertInBackground(vararg params: Path) {
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

    fun retrieveLatLngList(): LiveData<List<LatLngData>>? {

        return dbLatLngDataDao!!.getLatLng()
    }


    fun getPathNum(): Int {
        return dbPathDao!!.getLatestPathNum()
    }

    suspend fun generateNewPath(){
        InsertAsyncTask(dbPathDao).insertInBackground(Path(title = "Add title here"))
    }

}