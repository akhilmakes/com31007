package com.example.week_5B_solution

import android.app.Application
import com.example.week_5B_solution.model.AppDatabase

class ImageApplication: Application() {
    val databaseObj: AppDatabase by lazy { AppDatabase.getInstance(this) }
}