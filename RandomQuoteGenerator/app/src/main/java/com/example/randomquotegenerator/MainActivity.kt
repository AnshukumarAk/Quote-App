package com.example.randomquotegenerator

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.randomquotegenerator.ApiServices.QuotableApiService
import com.example.randomquotegenerator.Response.QuoteResponse
import com.example.randomquotegenerator.databinding.ActivityMainBinding
import com.github.ybq.android.spinkit.style.Circle
import com.github.ybq.android.spinkit.style.Wave
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var quotableApiService: QuotableApiService
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        retrofit = Retrofit.Builder()
            .baseUrl("http://api.quotable.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        quotableApiService = retrofit.create(QuotableApiService::class.java)


        if (isInternetAvailable(this@MainActivity)) {
            fetchDataFromApi()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Please Check your internet connection..",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.btnGenerateQuote.setOnClickListener {
            if (isInternetAvailable(this@MainActivity)) {
                fetchDataFromApi()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please Check your internet connection..",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    }

    private fun fetchDataFromApi() {
        // Show loading indicator
        binding.spinKit.visibility = View.VISIBLE

        val call = quotableApiService.getRandomQuote()
        call.enqueue(object : Callback<QuoteResponse> {
            override fun onResponse(call: Call<QuoteResponse>, response: Response<QuoteResponse>) {
                if (response.isSuccessful) {
                    val quoteResponse = response.body()
                    if (quoteResponse != null) {
                        val content = quoteResponse.content
                        val author = quoteResponse.author

                        // Update UI on the main thread
                        runOnUiThread {
                            binding.tvQuote.text = content
                            binding.tvAuthorName.text = "Author: $author"
                        }
                    }
                } else {
                    // Log the error response
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("API_ERROR", "Error: $errorMessage")

                    runOnUiThread {
                        binding.tvQuote.text = "Failed to fetch quote."
                    }
                }

                // Hide loading indicator
                runOnUiThread {
                    binding.spinKit.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<QuoteResponse>, t: Throwable) {
                Log.e("API_FAILURE", "Error message: ${t.message}", t)
                runOnUiThread {
                    binding.spinKit.visibility = View.GONE
                    binding.tvQuote.text = "Failed to fetch data. Please try again."
                }
            }
        })
    }


    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }

    }
}