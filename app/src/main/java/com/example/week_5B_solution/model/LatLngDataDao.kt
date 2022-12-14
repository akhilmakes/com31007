package com.example.week_5B_solution.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LatLngDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(latLngData: LatLngData)

    @Update
    suspend fun update(latLngData: LatLngData)

    @Delete
    suspend fun delete(latLngData: LatLngData)

    // For the camera move
    @Query("Select * from latlng where pathID = :pathID ORDER by id DESC LIMIT 1")
    fun getLatLng(pathID: Int): LatLngData

    // For the whole path
    @Query("Select * from latlng Where pathID = :id")
    fun getItem(id: Int): List<LatLngData>

    // For the markers
    @Query("select * from latlng A LEFT JOIN path B On pathID = pathID group by pathID")
    fun getOneLatLngFromPath(): List<LocationTitle>


    @Query("select * from latlng GROUP BY pathID")
    fun getALatLngFromPath(): LiveData<List<LatLngData>>



}