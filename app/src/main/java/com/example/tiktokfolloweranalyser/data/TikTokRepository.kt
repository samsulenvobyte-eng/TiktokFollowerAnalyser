package com.example.tiktokfolloweranalyser.data

import android.content.ContentResolver
import android.net.Uri
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

class TikTokRepository(private val dao: com.example.tiktokfolloweranalyser.data.database.TikTokDao) {

    suspend fun processZipFile(uri: Uri, contentResolver: ContentResolver): Result<TikTokUserData> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(Exception("Cannot open file"))

                ZipInputStream(inputStream).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while (entry != null) {
                        if (entry.name.endsWith("user_data_tiktok.json")) {
                            val reader = BufferedReader(InputStreamReader(zipInputStream))
                            val jsonString = reader.readText()
                            val userData = Gson().fromJson(jsonString, TikTokUserData::class.java)
                            
                            // Save to Database
                            saveToDatabase(userData)
                            
                            return@withContext Result.success(userData)
                        }
                        // Close current entry and move to next
                        zipInputStream.closeEntry()
                        entry = zipInputStream.nextEntry
                    }
                }
                Result.failure(Exception("JSON file not found in zip"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun saveToDatabase(userData: TikTokUserData) {
        val profile = userData.profileAndSettings.profileInfo.profileMap
        val snapshot = com.example.tiktokfolloweranalyser.data.database.SnapshotEntity(
            timestamp = System.currentTimeMillis(),
            username = profile.userName,
            region = profile.accountRegion,
            followerCount = profile.followerCount,
            followingCount = profile.followingCount,
            profilePhotoUrl = profile.profilePhoto
        )

        val relations = mutableListOf<com.example.tiktokfolloweranalyser.data.database.RelationEntity>()
        
        userData.profileAndSettings.follower.fansList?.forEach { user ->
             relations.add(com.example.tiktokfolloweranalyser.data.database.RelationEntity(
                 snapshotId = 0, // Will be set by DAO
                 username = user.userName,
                 date = user.date,
                 type = com.example.tiktokfolloweranalyser.data.database.RelationType.FOLLOWER
             ))
        }

        userData.profileAndSettings.following.followingList?.forEach { user ->
            relations.add(com.example.tiktokfolloweranalyser.data.database.RelationEntity(
                snapshotId = 0, // Will be set by DAO
                username = user.userName,
                date = user.date,
                type = com.example.tiktokfolloweranalyser.data.database.RelationType.FOLLOWING
            ))
        }

        dao.insertData(snapshot, relations)
    }
}
