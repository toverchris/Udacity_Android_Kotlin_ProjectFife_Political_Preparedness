package com.example.android.politicalpreparedness.election

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import java.util.*


class VoterInfoViewModel(application: Application) : ViewModel() {

    private val context = application.applicationContext

    //TODO: Add live data to hold voter info
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private  val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    @InternalCoroutinesApi
    private val database = ElectionDatabase.getInstance(application.applicationContext)

    private  val _upcomingElectionsFromApi = MutableLiveData<List<Election>>()
    val upcomingElectionsFromApi: LiveData<List<Election>>
        get() = _upcomingElectionsFromApi

    private  val _savedElectionsFromDatabase = MutableLiveData<List<Election>>()
    val savedElectionsFromDatabase: LiveData<List<Election>>
        get() = _savedElectionsFromDatabase

    init {
        //getVoterInfoFromApi()
        populateSavedElectionsfromDatabase()
        populateUpcomingElectionsfromApi()
    }

    fun getVoterInfoFromApi(division: Division, electionID: Int){

        coroutineScope.launch {
            try {
                var address : String
                when(division.state){
                    // update this list if some state is not know (like ga for georgia or an empty string)
                    "" -> address = "Michigan"
                    "ga" -> address = "georgia"
                    else    -> address = division.state
                }

                CivicsApi.retrofitService.getVoterInfo(address, electionID)
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
        //_savedElectionsFromDatabase.value = listOf(Election(88888, "VIP Test 999", Date(2022 - 1900, 1, 24), Division("123", "Sagres", "Portugal")))
    }

    //TODO: Add var and methods to populate voter info
    fun populateVoterInfo(id: Int, name: String, date: Date, division: Division){
        //_voterInfo.value = Election(id,name,date,division)
    }

    //TODO: Add var and methods to support loading URLs
    fun loadAddressURL(){
        val loadURL = _voterInfo.value?.state?.get(0)?.electionAdministrationBody?.electionInfoUrl
        Log.i("Load address url", loadURL.toString())
        if(!loadURL.isNullOrEmpty()){
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(loadURL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No application can handle this request."
                        + " Please install a webbrowser", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    fun loadBallotInfoURL(){
        val loadURL = _voterInfo.value?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl
        Log.i("Load ballot url", loadURL.toString())
        if(!loadURL.isNullOrEmpty()){
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(loadURL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No application can handle this request."
                        + " Please install a webbrowser", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    //TODO: Add var and methods to save and remove elections to local database
    @InternalCoroutinesApi
    fun saveToDatabase(){
        voterInfo.value?.let { database.electionDao.insert(it.election) }
        Log.i("saved to database", _voterInfo.value!!.election.toString())
    }
    @InternalCoroutinesApi
    fun removeFromDatabase(){
        voterInfo.value?.let { database.electionDao.delete(it.election) }
        Log.i("removed from database", _voterInfo.value!!.election.toString())
    }

    //TODO: cont'd -- Populate initial state of save button to reflect proper action based on election saved status
    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */
    fun updateButtonText(value: String): String{
        if(value == "Follow election"){
            return "Unfollow election"
        }
        else {
            return "Follow election"
        }
    }

}