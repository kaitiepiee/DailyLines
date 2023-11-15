package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Context
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class RequestManager (mContext: Context) {
    private var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://type.fit/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    fun getAllQuotes(listener: QuotesResponseListener){
        val call = retrofit.create(CallQuotes::class.java).CallQuotes() // double check
        call.enqueue(object : Callback<List<QuotesResponse>>{
            override fun onResponse(
                call: Call<List<QuotesResponse>>,
                response: Response<List<QuotesResponse>>
            ) {
                if (!response.isSuccessful){
                    listener.didError(response.message())
                    return
                }
                response.body()?.let { quotesList ->
                    if (quotesList.isNotEmpty()) {
                        Log.d("RequestManager", "Received quotes from API: $quotesList")
                        listener.didFetch(quotesList, response.message(), "additionalMessage")
                    } else {
                        Log.e("RequestManager", "API call not successful. Response: ${response.message()}")
                        listener.didError("No quotes found.")
                    }
                }
            }

            override fun onFailure(call: Call<List<QuotesResponse>>, t: Throwable) {
                Log.e("RequestManager", "API call failed. Error: ${t.message}")
                t.message?.let { listener.didError(it) }
            }

        })
    }
    private interface CallQuotes {
        @GET("api/quotes")
        fun CallQuotes(): Call<List<QuotesResponse>>
    }
}

// source: https://www.youtube.com/watch?v=2k7aHIDQc1s&ab_channel=CodingWithEvan