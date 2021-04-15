package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDatabase
//import com.example.android.politicalpreparedness.database.asDomainModel
//import com.example.android.politicalpreparedness.database.asDatabaseModel
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import kotlinx.coroutines.*
import okhttp3.Callback
import okhttp3.Dispatcher
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.*


//TODO: Construct ViewModel and provide election datasource

@InternalCoroutinesApi
class ElectionsViewModel(application: Application): ViewModel() {

    private var viewModelJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private  val _upcomingElectionsList = MutableLiveData<List<Election>>()
    val upcomingElectionsList: LiveData<List<Election>>
        get() = _upcomingElectionsList

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
        viewModelScope.launch{
            //saveElectionToDatabase()
            //saveElectionsToDatabase()
        }
        viewModelScope.launch {
            getElectionsDataFromDatabase()
        }
    }

    private fun getElectionsDataFromApi(){
        coroutineScope.launch {
            try {
                CivicsApi.retrofitService.getElections()
                        .enqueue(object : retrofit2.Callback<ElectionResponse> {
                            override fun onResponse(call: Call<ElectionResponse>, response: Response<ElectionResponse>) {
                                Log.i("Download Success", response.body().toString())
                                _upcomingElectionsList.value = response.body()!!.elections
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

    private fun saveElectionsToDatabase(){
        val elections = listOf(
                Election(1, "VIP Test 1", Date(2022-1900,1,24), Division("123","Sagres","Portugal")),
                Election(2, "Test 2", Date(2022-1900,2,24), Division("222","Tonel","Portugal")),
                Election(3, "Test 3", Date(2022-1900,3,24), Division("333","Beliche","Portugal")),
                Election(4, "Test 4", Date(2022-1900,4,24), Division("444","Mareta","Portugal") ),
                Election(5, "Test 5", Date(2022-1900,5,24), Division("24442","Marthinal","Portugal"))
        )
        database.electionDao.insertAll(elections)
    }

    private fun saveElectionToDatabase(){
        val election = Election(1, "First Database object", Date(2022-1900,1,24), Division("123","Sagres","Portugal"))
        database.electionDao.insert(election)
    }

    private fun getElectionsDataFromDatabase(){
        val electionList = database.electionDao.getElectionsFromDatabase()
        Log.i("Database electionList", electionList.value.toString())
       _savedElectionsList.value = electionList.value
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