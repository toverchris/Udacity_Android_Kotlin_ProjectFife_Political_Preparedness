package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.*
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import java.util.*

class RepresentativeViewModel: ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private  val _representativesList = MutableLiveData<List<Representative>>()
    val representativesList: LiveData<List<Representative>>
        get() = _representativesList

    private  val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    fun getRepresentativesFromApi(address : String){
        coroutineScope.launch {
            try {
                CivicsApi.retrofitService.getRepresentatives(address)
                        .enqueue(object : retrofit2.Callback<RepresentativeResponse> {
                            override fun onResponse(call: Call<RepresentativeResponse>, response: Response<RepresentativeResponse>) {
                                if(response.body()!=null){
                                    Log.i("Download Success", response.body().toString())
                                    val (offices, officials) = response.body()!!
                                    _representativesList.value = offices.flatMap { office -> office.getRepresentatives(officials) }
                                }else{
                                    Log.e("RepresentativesDataFromApi", "Add your personal API key in the CivicsHttpClient class")
                                }
                            }
                            override fun onFailure(call: Call<RepresentativeResponse>, t: Throwable) {
                                Log.i("Download Failure", t.message.toString())
                            }
                        })
            }catch (e: Exception) {
                Log.i("Download Failure", e.message.toString())
            }
        }
    }

}
