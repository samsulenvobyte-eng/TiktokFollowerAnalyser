package com.example.tiktokfolloweranalyser.data.apify

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApifyApiService {

    @POST("acts/{actorId}/runs")
    suspend fun runActor(
        @Path("actorId") actorId: String,
        @Query("token") token: String,
        @Body request: ApifyRunRequest
    ): Response<ApifyRunResponse>

    @GET("actor-runs/{runId}")
    suspend fun getRunStatus(
        @Path("runId") runId: String,
        @Query("token") token: String
    ): Response<ApifyRunResponse>

    @GET("datasets/{datasetId}/items")
    suspend fun getDatasetItems(
        @Path("datasetId") datasetId: String,
        @Query("token") token: String
    ): Response<List<ApifyFollowerItem>>
}
