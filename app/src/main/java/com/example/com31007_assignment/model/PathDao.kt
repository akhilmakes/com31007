package com.example.com31007_assignment.model

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface PathDao {

    @Insert
    suspend fun insert(path: Path)

    @Update
    suspend fun update(path: Path)

    @Delete
    suspend fun delete(path: Path)

    @Query("Select id from path ORDER BY id DESC LIMIT 1")
    fun getLatestPathNum(): LiveData<Int>

    @Query("Select id from path ORDER BY id DESC LIMIT 1")
    fun getCurrentPathNum(): Int

    @Query("Select * from path where id = :id")
    fun getPath(id: Int): Path

    @Query("update path set title = :title where id=:id")
    fun updateTitle(title:String, id:Int)

    @Query("select title from path where id=:id")
    fun getTitle(id: Int) : String

    @Query("delete from path where id=:id")
    fun deletePath(id:Int)

}