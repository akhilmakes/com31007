package com.example.week_5B_solution.data

import androidx.room.*

@Entity(tableName = "image", indices=[Index(value=["id", "image_title"])])
data class ImageData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name="image_path") val imagePath: String,
    @ColumnInfo(name="image_title") var title: String,
    @ColumnInfo(name="image_description") var description: String? = null,
    @ColumnInfo(name="thumbnail_filename") var thumbnail: String? = null)
{ }