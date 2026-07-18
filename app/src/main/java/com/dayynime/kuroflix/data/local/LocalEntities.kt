package com.dayynime.kuroflix.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // composite id: "source:slug"
    val title: String,
    val thumbnail: String,
    val rating: String,
    val source: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val episodeId: String, // composite: "source:episode_slug"
    val animeId: String,               // composite or slug
    val animeTitle: String,
    val episodeTitle: String,
    val thumbnail: String,
    val progressMillis: Long,
    val durationMillis: Long,
    val source: String,
    val timestamp: Long = System.currentTimeMillis()
)
