package com.example.week_5B_solution.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ImageDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(imageData: ImageData): Long

    @Update
    suspend fun update(imageData: ImageData)

    @Delete
    suspend fun delete(imageData: ImageData)

    @Query("Select * from image ORDER by id ASC")
    suspend fun getItems(): List<ImageData>

    // Useful for tracking Entities
    @Query("Select * from image Where id = :id")
    suspend fun getItem(id: Int): ImageData

    @Query("Select * from image Where pathID = :pathID")
    suspend fun getAllPathImages(pathID: Int): List<ImageData>

    @Query("select * from image where (image_title like '%' || :search || '%' or image_description like '%' || :search || '%')")
    fun search(search : String) : List<ImageData>

}