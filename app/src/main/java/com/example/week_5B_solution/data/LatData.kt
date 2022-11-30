package com.example.week_5B_solution.data

import androidx.room.*

@Entity(tableName = "latlng")
data class LatData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name="lat") val lat: Double,
    @ColumnInfo(name="lng") var lng: Double)
{ }