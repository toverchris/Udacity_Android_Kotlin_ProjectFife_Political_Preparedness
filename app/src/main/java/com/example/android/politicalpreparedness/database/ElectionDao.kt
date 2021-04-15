package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Query("select * from election_table order by name")
    fun getElectionsFromDatabase(): LiveData<List<Election>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(election: Election)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(elections: List<Election>)

    //TODO: Add delete query

    //TODO: Add clear query

}
