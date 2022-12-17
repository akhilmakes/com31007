package com.example.com31007_assignment.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "latlng",
        foreignKeys = [
            ForeignKey(
                entity = Path::class,
                parentColumns = ["id"],
                childColumns = ["pathID"],
                onDelete = ForeignKey.CASCADE
            )]

)
data class LatLngData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name="lat") val lat: Double,
    @ColumnInfo(name="lng") val lng: Double,
    @ColumnInfo(name="air_pressure") val airPressure: Float? = null,
    @ColumnInfo(name="pathID") var pathID: Int?,
    )
{ }