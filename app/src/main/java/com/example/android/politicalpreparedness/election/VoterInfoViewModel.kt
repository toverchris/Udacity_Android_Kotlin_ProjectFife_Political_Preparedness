package com.example.android.politicalpreparedness.election

import android.annotation.SuppressLint
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


@InternalCoroutinesApi
class VoterInfoViewModel(application: Application) : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private  val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    @InternalCoroutinesApi
    private val database = ElectionDatabase.getInstance(application.applicationContext)

    private  val _upcomingElectionsFromApi = MutableLiveData<List<Election>>()
    val upcomingElectionsFromApi: LiveData<List<Election>>
        get() = _upcomingElectionsFromApi

    private  val _savedElectionsFromDatabase = MutableLiveData<List<Election>>()
    val savedElectionsFromDatabase: LiveData<List<Election>>
        get() = _savedElectionsFromDatabase

    init {
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
                                if(response.body()!=null){
                                    Log.i("Download Success", response.body().toString())
                                    _voterInfo.value = response.body()!!
                                } else{
                                    Log.e("RepresentativesDataFromApi", "Add your personal API key in the CivicsHttpClient class")
                                }
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

    @InternalCoroutinesApi
    private fun populateSavedElectionsfromDatabase() {
        _savedElectionsFromDatabase.value = database.electionDao.getElectionsFromDatabase()
    }

    fun loadAddressURL(){
        val loadURL = _voterInfo.value?.state?.get(0)?.electionAdministrationBody?.electionInfoUrl
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

    fun updateButtonText(value: String): String{
        if(value == "Follow election"){
            return "Unfollow election"
        }
        else {
            return "Follow election"
        }
    }
}