package com.example.android.politicalpreparedness.network

import com.example.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


private const val BASE_URL = "https://www.googleapis.com/civicinfo/v2/"

var customDateAdapter: Any = object : Any() {
    var dateFormat: DateFormat? = null

    @ToJson
    @Synchronized
    fun dateToJson(d: Date?): String? {
        return dateFormat!!.format(d)
    }

    @FromJson
    @Synchronized
    @Throws(ParseException::class)
    fun dateToJson(s: String?): Date? {
        return dateFormat!!.parse(s)
    }

    init {
        dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateFormat!!.timeZone = TimeZone.getTimeZone("GMT")
    }
}
private val moshi = Moshi.Builder()
        .add(ElectionAdapter())
        .add(KotlinJsonAdapterFactory())
        .add(customDateAdapter)
        .build()

private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(CivicsHttpClient.getClient())
        .baseUrl(BASE_URL)
        .build()

/**
 *  Documentation for the Google Civics API Service can be found at https://developers.google.com/civic-information/docs/v2
 */

interface CivicsApiService {

    @GET("elections")
    fun getElections(): Call<ElectionResponse>

    @GET("voterinfo")
    fun getVoterInfo(
            @Query("address") address : String,
            @Query("electionId") electionId : Int
    ): Call<VoterInfoResponse>

    @GET("representatives")
    fun getRepresentatives(@Query("address") address : String): Call<RepresentativeResponse>
}

object CivicsApi {
    val retrofitService: CivicsApiService by lazy {
        retrofit.create(CivicsApiService::class.java)
    }
}