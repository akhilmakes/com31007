package com.example.com31007.data

import android.content.Context
import androidx.room.*

abstract class ImageRoomDatabase: RoomDatabase() {

    abstract fun imageDataDao(): ImageDataDao

    companion object{
        @Volatile
        private var INSTANCE: ImageRoomDatabase? = null
        fun getDatabase(context: Context): ImageRoomDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImageRoomDatabase::class.java,
                    "room_database"

                )
                    // Wipes and rebuilds instead of migrating if no Migration object specified.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                return instance
            }
        }
    }
}