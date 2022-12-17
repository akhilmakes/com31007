package com.example.com31007_assignment

import android.app.Application
import com.example.com31007_assignment.model.AppDatabase

class ImageApplication: Application() {
    val databaseObj: AppDatabase by lazy { AppDatabase.getInstance(this) }
}