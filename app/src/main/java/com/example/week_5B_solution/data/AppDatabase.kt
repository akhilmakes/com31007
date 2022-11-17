package com.example.week_5B_solution.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ImageData::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun imageDataDao(): ImageDataDao

    companion object{
        private val DATABASE_NAME = "Lab5Db"
        // For Singleton instantiation
        @Volatile private var db_instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return db_instance ?: synchronized(this) {
                db_instance ?: buildDatabase(context).also { db_instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    // Wipes and rebuilds instead of migrating if no Migration object specified.
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}