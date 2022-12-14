package com.example.week_5B_solution.model

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

//    @Query("update path set title = :title where id=:id")
//    fun updateTitle(title:String, id:Int)

}