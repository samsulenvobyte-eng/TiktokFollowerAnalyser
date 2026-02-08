package com.example.tiktokfolloweranalyser.data.apify

import com.google.gson.annotations.SerializedName

data class ApifyRunRequest(
    val handles: List<String>,
    val maxFollowersPerProfile: Int = 20,
    val proxyConfiguration: ProxyConfiguration = ProxyConfiguration()
)

data class ProxyConfiguration(
    val useApifyProxy: Boolean = true
)

data class ApifyRunResponse(
    val data: ApifyRunData
)

data class ApifyRunData(
    val id: String,
    val defaultDatasetId: String,
    val status: String
)

data class ApifyFollowerItem(
    @SerializedName("unique_id") val uniqueId: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("avatar_thumb") val avatarThumb: AvatarThumb?,
    @SerializedName("follower_count") val followerCount: Int?,
    @SerializedName("following_count") val followingCount: Int?
)

data class AvatarThumb(
    @SerializedName("url_list") val urlList: Map<String, String>?
)
