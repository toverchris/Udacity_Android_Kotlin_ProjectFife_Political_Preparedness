package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Query("SELECT * FROM election_table")
    fun getElectionsFromDatabase(): LiveData<List<Election>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(election: Election)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(elections: List<Election>)

    @Delete
    fun delete(election: Election)

    //TODO: Add clear query

}
