package com.example.com31007

import android.app.Application
import com.example.com31007.data.ImageRoomDatabase

class ImageApplication: Application() {
    val databaseObj: ImageRoomDatabase by lazy { ImageRoomDatabase.getDatabase(this) }
}