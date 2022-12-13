package com.example.week_5B_solution.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class LocationTitle(
    @PrimaryKey(autoGenerate = false) var pathID: Int,
    // @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name="lat") val lat: Double,
    @ColumnInfo(name="lng") val lng: Double,
    @ColumnInfo(name = "title") var title: String
)
