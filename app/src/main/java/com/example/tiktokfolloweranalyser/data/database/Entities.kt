package com.example.tiktokfolloweranalyser.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val username: String,
    val region: String?,
    val followerCount: Int?,
    val followingCount: Int?,
    val profilePhotoUrl: String?
)

@Entity(
    tableName = "relations",
    foreignKeys = [
        ForeignKey(
            entity = SnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["snapshotId"])]
)
data class RelationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val snapshotId: Long,
    val username: String,
    val date: String,
    val type: RelationType // FOLLOWER or FOLLOWING
)

enum class RelationType {
    FOLLOWER,
    FOLLOWING
}
