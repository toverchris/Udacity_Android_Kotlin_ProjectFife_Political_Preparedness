/*
package com.example.android.politicalpreparedness.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import java.util.*

@Entity
data class DatabaseElection constructor(
        @PrimaryKey
        val id: Int,
        val name: String,
        val electionDay: Date,
        val division: Division
)

fun List<DatabaseElection>.asDomainModel(): List<Election> {
    return map {
        Election(
                id = it.id,
                name =  it.name,
                electionDay =  it.electionDay,
                division = it.division
        )
    }
}

fun List<Election>.asDatabaseModel(): Array<DatabaseElection> {
    return map {
        DatabaseElection(
                id = it.id,
                name =  it.name,
                electionDay = it.electionDay,
                division = it.division
        )
    }.toTypedArray()
}*/
