package com.example.com31007_assignment.model

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
    @Query("select * from latlng A LEFT JOIN path B On (A.pathID = B.id) group by pathID")
    fun getOneLatLngFromPath(): List<LocationTitle>

    @Query("Select air_pressure from latlng order by id desc limit 1")
    suspend fun getLatestPressureReading(): Float



}