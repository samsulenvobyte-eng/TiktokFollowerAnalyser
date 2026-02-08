package com.example.tiktokfolloweranalyser.data.apify

import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApifyRepository {

    private val apiService: ApifyApiService

    init {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.apify.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        apiService = retrofit.create(ApifyApiService::class.java)
    }

    suspend fun scrapeFollowers(handle: String, maxCount: Int): Result<List<ApifyFollowerItem>> {
        return try {
            // Hardcoded token for test project
            val token = "apify_api_exa3CaLD0u4vUGnedS0cwWUJAXBdNE24I2C2"
            
            // 1. Run the Actor
            val runRequest = ApifyRunRequest(
                handles = listOf(handle),
                maxFollowersPerProfile = maxCount
            )
            // Using the specific actor ID for TikTok Follower Scraper (6yigNBEXrmqY7bTAl)
            val actorId = "6yigNBEXrmqY7bTAl" 
            
            val runResponse = apiService.runActor(actorId, token, runRequest)
            if (!runResponse.isSuccessful || runResponse.body() == null) {
                return Result.failure(Exception("Failed to start actor: ${runResponse.errorBody()?.string()}"))
            }

            val runData = runResponse.body()!!.data
            val runId = runData.id
            val datasetId = runData.defaultDatasetId

            // 2. Poll for Completion
            // TikTok scraping can take time. We poll status.
            var status = runData.status
            var attempts = 0
            while (status != "SUCCEEDED" && status != "FAILED" && status != "ABORTED" && attempts < 30) {
                delay(3000) // Wait 3 seconds
                val statusResponse = apiService.getRunStatus(runId, token)
                if (statusResponse.isSuccessful && statusResponse.body() != null) {
                    status = statusResponse.body()!!.data.status
                }
                attempts++
            }

            if (status != "SUCCEEDED") {
                 return Result.failure(Exception("Actor run failed or timed out. Status: $status"))
            }

            // 3. Get Results
            val itemsResponse = apiService.getDatasetItems(datasetId, token)
            if (itemsResponse.isSuccessful && itemsResponse.body() != null) {
                 Result.success(itemsResponse.body()!!)
            } else {
                 return Result.failure(Exception("Failed to fetch dataset items: ${itemsResponse.errorBody()?.string()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
