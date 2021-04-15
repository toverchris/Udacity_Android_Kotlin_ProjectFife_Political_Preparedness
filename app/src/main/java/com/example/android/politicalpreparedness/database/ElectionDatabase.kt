package com.example.android.politicalpreparedness.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.InternalCoroutinesApi

@Database(entities = [Election::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ElectionDatabase: RoomDatabase() {

    abstract val electionDao: ElectionDao

    companion object {

        @Volatile
        private var INSTANCE: ElectionDatabase? = null

        @InternalCoroutinesApi
        fun getInstance(context: Context): ElectionDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            ElectionDatabase::class.java,
                            "election_database"
                    )
                            .allowMainThreadQueries()               // TODO: 4/15/21 That has to be gone due to bad programming on the main threat. Just for help now 
                            .fallbackToDestructiveMigration()
                            .build()

                    INSTANCE = instance
                }

                return instance
            }
        }

    }

}