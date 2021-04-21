package com.example.android.politicalpreparedness.database

import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Query("Select * from election_table order by electionDay")
    fun getElectionsFromDatabase(): List<Election>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(election: Election)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(elections: List<Election>)

    @Delete
    fun delete(election: Election)

    @Query("Delete from election_table")
    fun clear()
}
