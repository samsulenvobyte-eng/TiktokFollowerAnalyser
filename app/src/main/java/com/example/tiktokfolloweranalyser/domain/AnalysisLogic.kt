package com.example.tiktokfolloweranalyser.domain

import com.example.tiktokfolloweranalyser.data.TikTokUserData
import com.example.tiktokfolloweranalyser.data.User

object AnalysisLogic {

    fun getNotFollowingBack(userData: TikTokUserData): List<User> {
        val followers = userData.profileAndSettings.follower.fansList?.map { it.userName }?.toSet() ?: emptySet()
        val following = userData.profileAndSettings.following.followingList ?: emptyList()

        return following.filter { user ->
            !followers.contains(user.userName)
        }
    }
}
