package com.example.week_5B_solution.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface LatDataDao {
    @Query("Select * from latlng")
    suspend fun getLatLng(): List<LatData>

    // Useful for tracking Entities
    @Query("Select * from latlng Where id = :id")
    suspend fun getItem(id: Int): LatData

    @Query("Select lat, lng from latlng where pathNum = :pathNum")
    suspend fun getPath(pathNum: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(latData: LatData)

    @Update
    suspend fun update(latData: LatData)

    @Delete
    suspend fun delete(latData: LatData)

}