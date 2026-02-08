package com.example.tiktokfolloweranalyser.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tiktokfolloweranalyser.data.database.SnapshotEntity
import com.example.tiktokfolloweranalyser.data.database.RelationEntity
import com.example.tiktokfolloweranalyser.data.database.TikTokDao

@Database(entities = [SnapshotEntity::class, RelationEntity::class], version = 1, exportSchema = false)
abstract class TikTokDatabase : RoomDatabase() {
    abstract fun tikTokDao(): TikTokDao

    companion object {
        @Volatile
        private var INSTANCE: TikTokDatabase? = null

        fun getDatabase(context: Context): TikTokDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TikTokDatabase::class.java,
                    "tiktok_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
