package com.dayynime.kuroflix.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "kuroflix_settings")

/**
 * Preferensi default server streaming — GLOBAL, berlaku lintas 4 sumber data
 * (Animasu/Samehadaku/Animekompi/Donghua), karena daftar server tiap episode
 * dari semua sumber udah dinormalisasi jadi satu bentuk yang sama (VideoServer),
 * jadi cukup 1 preferensi buat "server favorit" tanpa perlu dipisah per-source.
 *
 * Cara kerja matching: `server.name` (label dari API, mis. "Mp4Upload", "Server 1 - Filedon")
 * dicek apakah MENGANDUNG keyword pilihan user (case-insensitive). Kalau ketemu,
 * itu yang dipilih otomatis pas buka episode; kalau gak ketemu server dengan keyword
 * itu di episode tsb, fallback ke server pertama di list (behavior lama).
 */
object PreferredServerOptions {
    // value = keyword buat dicocokin ke server.name, label = ditampilin di UI
    val ALL = listOf(
        "" to "Otomatis (server pertama)",
        "mp4upload" to "Mp4Upload",
        "pixeldrain" to "Pixeldrain",
        "filedon" to "Filedon",
        "streamtape" to "Streamtape",
        "mediafire" to "Mediafire",
        "wibufile" to "Wibufile",
        "filemoon" to "Filemoon",
        "vidhide" to "Vidhide",
        "blogger" to "Blogger/Google Drive",
        "gdrive" to "GDrive",
        "dailymotion" to "Dailymotion",
        "ok.ru" to "OK.ru"
    )
}

class PreferencesManager(private val context: Context) {

    private val PREFERRED_SERVER_KEY = stringPreferencesKey("preferred_server_keyword")

    val preferredServerKeyword: Flow<String> = context.settingsDataStore.data
        .map { prefs -> prefs[PREFERRED_SERVER_KEY] ?: "" }

    suspend fun setPreferredServer(keyword: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[PREFERRED_SERVER_KEY] = keyword
        }
    }

    /**
     * Cari server yang cocok sama preferensi user dari daftar server 1 episode.
     * Return null kalau preferensi kosong ("Otomatis") atau gak ada server yang cocok
     * -> caller fallback ke servers.first() seperti behavior lama.
     */
    fun findPreferred(servers: List<com.dayynime.kuroflix.data.model.VideoServer>, keyword: String) =
        if (keyword.isBlank()) null
        else servers.firstOrNull { it.name.contains(keyword, ignoreCase = true) }
}
