package com.example.com31007.data
import androidx.room.*


@Dao
interface ImageDataDao {
    @Query("SELECT * from image ORDER by id ASC")
    suspend fun getItems(): List<ImageData>

    @Query("SELECT * from image WHERE id = :id")
    fun getItem(id: Int): ImageData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(singleImageData: ImageData): Long

    @Update
    suspend fun update(imageData: ImageData)

    @Delete
    suspend fun delete(imageData: ImageData)
}