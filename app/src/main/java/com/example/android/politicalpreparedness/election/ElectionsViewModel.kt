package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import java.util.*

@InternalCoroutinesApi
class ElectionsViewModel(application: Application): ViewModel() {

    private var viewModelJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private  val _upcomingElectionsList = MutableLiveData<List<Election>>()
    val upcomingElectionsList: LiveData<List<Election>>
        get() = _upcomingElectionsList

    private  val _upcomingElectionsListSource = MutableLiveData<List<Election>>()
    val upcomingElectionsListSource: LiveData<List<Election>>
        get() = _upcomingElectionsListSource

    private  val _savedElectionsList = MutableLiveData<List<Election>>()
    val savedElectionsList: LiveData<List<Election>>
        get() = _savedElectionsList

    private  val _navigateToSelectedElection = MutableLiveData<Election>()
    val navigateToSelectedElection: LiveData<Election>
        get() = _navigateToSelectedElection

    @InternalCoroutinesApi
    private val database = ElectionDatabase.getInstance(application.applicationContext)

    init {
        getElectionsDataFromApi()
        viewModelScope.launch {
            getElectionsDataFromDatabase()
            hideUpcomingElectionsIfSavedInDatabase()
        }
    }

    fun hideUpcomingElectionsIfSavedInDatabase(){
        if(!savedElectionsList.value.isNullOrEmpty() && !upcomingElectionsListSource.value.isNullOrEmpty()){
            _upcomingElectionsList.value = _upcomingElectionsListSource.value
            _upcomingElectionsList.value = _upcomingElectionsList.value!!.minus(savedElectionsList.value!!)
        }
    }

    private fun getElectionsDataFromApi(){
        coroutineScope.launch {
            try {
                CivicsApi.retrofitService.getElections()
                        .enqueue(object : retrofit2.Callback<ElectionResponse> {
                            override fun onResponse(call: Call<ElectionResponse>, response: Response<ElectionResponse>) {
                                if(response.body()!=null){
                                    Log.i("Download Success", response.body().toString())
                                    _upcomingElectionsList.value = response.body()!!.elections
                                    _upcomingElectionsListSource.value = _upcomingElectionsList.value
                                }else{
                                    Log.e("ElectionsDataFromApi", "Add your personal API key in the CivicsHttpClient class")
                                    _upcomingElectionsList.value = listOf(Election(0,"Add your personal API key in the CivicsHttpClient class",Date(), Division("","","")))
                                }
                            }
                            override fun onFailure(call: Call<ElectionResponse>, t: Throwable) {
                                Log.i("Download Failure", t.message.toString())
                            }
                        })
            }catch (e: Exception) {
                Log.i("Download Failure", e.message.toString())
            }
        }
    }

    fun getElectionsDataFromDatabase(){
        val electionList : List<Election> = database.electionDao.getElectionsFromDatabase()
        _savedElectionsList.value = electionList
    }

    fun displayElection(election: Election) {
        _navigateToSelectedElection.value = election
    }

    fun displayElectionDetailsComplete() {
        _navigateToSelectedElection.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}