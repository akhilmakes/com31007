package com.example.week_5B_solution.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(tableName = "image", indices=[Index(value=["id", "image_title", "pathID"])],
    foreignKeys = [
        ForeignKey(
            entity = Path::class,
            parentColumns = ["id"],
            childColumns = ["pathID"],
            onDelete = CASCADE
    )]
)
data class ImageData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name="image_path") val imagePath: String,
    @ColumnInfo(name="image_title") var title: String,
    @ColumnInfo(name="image_description") var description: String? = null,
    @ColumnInfo(name="thumbnail_filename") var thumbnail: String? = null,
    @ColumnInfo(name= "pathID") val pathID: Int
)