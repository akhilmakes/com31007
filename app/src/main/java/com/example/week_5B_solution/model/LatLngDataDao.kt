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

    @Query("Select * from latlng where pathID = :pathID ORDER by id DESC LIMIT 1")
    fun getLatLng(pathID: Int): LiveData<LatLngData>?

    // Useful for tracking Entities
    @Query("Select * from latlng Where id = :id")
    suspend fun getItem(id: Int): LatLngData


    @Query("select * from latlng A LEFT JOIN path B On pathID = pathID group by pathID")
    fun getOnePathData(): LiveData<List<LocationTitle>>



}