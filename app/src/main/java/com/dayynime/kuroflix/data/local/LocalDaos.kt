package com.dayynime.kuroflix.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id LIMIT 1)")
    fun isBookmarked(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE animeId = :animeId ORDER BY timestamp DESC")
    fun getHistoryForAnime(animeId: String): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE episodeId = :episodeId LIMIT 1")
    suspend fun getHistoryForEpisode(episodeId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE episodeId = :episodeId")
    suspend fun deleteHistory(episodeId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}
