package com.example.randomquotegenerator.ApiServices

import com.example.randomquotegenerator.Response.QuoteResponse
import retrofit2.Call
import retrofit2.http.GET

interface QuotableApiService {
    @GET("random")
    fun getRandomQuote(): Call<QuoteResponse>

}