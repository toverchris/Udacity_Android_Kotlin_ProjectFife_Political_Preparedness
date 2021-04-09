package com.example.android.politicalpreparedness.election

import android.app.Application
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.MainActivity
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import java.util.*
import kotlin.coroutines.coroutineContext

class VoterInfoViewModel(application: Application) : ViewModel() {

    //TODO: Add live data to hold voter info
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private  val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    private val database = ElectionDatabase.getInstance(application)

    private  val _upcomingElectionsFromApi = MutableLiveData<List<Election>>()
    val upcomingElectionsFromApi: LiveData<List<Election>>
        get() = _upcomingElectionsFromApi

    private  val _savedElectionsFromDatabase = MutableLiveData<List<Election>>()
    val savedElectionsFromDatabase: LiveData<List<Election>>
        get() = _savedElectionsFromDatabase

    init {
        getVoterInfoFromApi()
        populateSavedElectionsfromDatabase()
        populateUpcomingElectionsfromApi()
    }

    private fun getVoterInfoFromApi(){
        coroutineScope.launch {
            try {
                CivicsApi.retrofitService.getVoterInfo("Michigan",2000)
                        .enqueue(object : retrofit2.Callback<VoterInfoResponse> {
                            override fun onResponse(call: Call<VoterInfoResponse>, response: Response<VoterInfoResponse>) {
                                Log.i("Download Success", response.body().toString())
                                _voterInfo.value = response.body()!!
                            }
                            override fun onFailure(call: Call<VoterInfoResponse>, t: Throwable) {
                                Log.i("Download Failure", t.message.toString())
                            }

                        })
            }catch (e: Exception) {
                Log.i("Download Failure", e.message.toString())
            }
        }
    }

    private fun populateUpcomingElectionsfromApi() {
    }

    private fun populateSavedElectionsfromDatabase() {
        _savedElectionsFromDatabase.value = listOf(Election(88888, "VIP Test 999", Date(2022 - 1900, 1, 24), Division("123", "Sagres", "Portugal")))
    }

    //TODO: Add var and methods to populate voter info
    fun populateVoterInfo(id: Int, name: String, date: Date , division: Division){
        //_voterInfo.value = Election(id,name,date,division)
    }

    //TODO: Add var and methods to support loading URLs

    //TODO: Add var and methods to save and remove elections to local database
    fun updateButtonText(value: String): String{
        if(value == "Follow election"){
            return "Unfollow election"
        }
        else {
            return "Follow election"
        }
    }

    fun saveToDatabase(){
        _savedElectionsFromDatabase.value!!.plus(_voterInfo.value)
        Log.i("saved to database", _savedElectionsFromDatabase.value.toString())
    }
    fun removeFromDatabase(){
        _savedElectionsFromDatabase.value!!.minus(_voterInfo)
        Log.i("removed from database", _savedElectionsFromDatabase.value.toString())
    }

    //TODO: cont'd -- Populate initial state of save button to reflect proper action based on election saved status

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

}