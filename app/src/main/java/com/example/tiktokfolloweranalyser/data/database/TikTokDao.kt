package com.example.tiktokfolloweranalyser.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TikTokDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: SnapshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(relations: List<RelationEntity>)

    @Transaction // Ensure atomicity
    suspend fun insertData(snapshot: SnapshotEntity, relations: List<RelationEntity>) {
        val snapshotId = insertSnapshot(snapshot) // Insert snapshot and get ID
        val relationsWithId = relations.map { it.copy(snapshotId = snapshotId) } // Update relations with snapshot ID
        insertRelations(relationsWithId)
    }

    @Query("SELECT * FROM snapshots ORDER BY timestamp DESC")
    suspend fun getAllSnapshots(): List<SnapshotEntity>

    @Query("SELECT * FROM relations WHERE snapshotId = :snapshotId")
    suspend fun getRelationsForSnapshot(snapshotId: Long): List<RelationEntity>
}
