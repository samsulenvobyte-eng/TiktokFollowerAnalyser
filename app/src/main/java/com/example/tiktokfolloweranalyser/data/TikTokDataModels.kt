package com.example.tiktokfolloweranalyser.data

import com.google.gson.annotations.SerializedName

data class TikTokUserData(
    @SerializedName("Profile And Settings")
    val profileAndSettings: ProfileAndSettings
)

data class ProfileAndSettings(
    @SerializedName("Follower")
    val follower: Follower,
    @SerializedName("Following")
    val following: Following,
    @SerializedName("Profile Info")
    val profileInfo: ProfileInfo,
    @SerializedName("Settings")
    val settings: Settings
)

data class Follower(
    @SerializedName("FansList")
    val fansList: List<User>?
)

data class Following(
    @SerializedName("Following")
    val followingList: List<User>?
)

data class User(
    @SerializedName("Date")
    val date: String,
    @SerializedName("UserName")
    val userName: String
)

data class ProfileInfo(
    @SerializedName("ProfileMap")
    val profileMap: ProfileMap
)

data class ProfileMap(
    @SerializedName("PlatformInfo")
    val platformInfo: List<PlatformInfo>?,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("accountRegion")
    val accountRegion: String?,
    @SerializedName("followerCount")
    val followerCount: Int?,
    @SerializedName("followingCount")
    val followingCount: Int?,
    @SerializedName("profilePhoto")
    val profilePhoto: String?

)

data class PlatformInfo(
    @SerializedName("Name")
    val name: String,
    @SerializedName("Platform")
    val platform: String
)

data class Settings(
    @SerializedName("SettingsMap")
    val settingsMap: Map<String, Any>
)
