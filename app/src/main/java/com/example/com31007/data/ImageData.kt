package com.example.com31007.data
import androidx.room.*

@Entity(tableName = "image", )
data class ImageData(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name="uri") val imageUri: String,
    @ColumnInfo(name="title") var imageTitle: String,
    @ColumnInfo(name="description") var imageDescription: String? = null,

)


