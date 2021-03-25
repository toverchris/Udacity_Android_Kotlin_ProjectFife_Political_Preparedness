package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//TODO: Construct ViewModel and provide election datasource


class ElectionsViewModel: ViewModel() {

    //TODO: Create live data val for upcoming elections
    private  val _upcomingElectionsList = MutableLiveData<List<String>>()
    val upcomingElectionsList: LiveData<List<String>>
        get() = _upcomingElectionsList

    //TODO: Create live data val for saved elections
    private  val _savedElectionsList = MutableLiveData<List<String>>()
    val savedElectionsList: LiveData<List<String>>
        get() = _savedElectionsList

    init {
        defineFakeData()
    }

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    fun defineFakeData(){
        _upcomingElectionsList.value = listOf("Test 1", "Test 2", "Test 3", "Test 4")
        _savedElectionsList.value = listOf("Test 5", "Test 6", "Test 7", "Test 8")
    }

    //TODO: Create functions to navigate to saved or upcoming election voter info

}