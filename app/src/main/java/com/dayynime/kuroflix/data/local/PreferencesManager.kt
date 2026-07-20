package com.dayynime.kuroflix.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
 * Nama server yang bisa dipilih diambil LANGSUNG dari data API anime yang
 * lagi dibuka (lihat AnimeViewModel.loadAvailableServers), bukan daftar
 * statis — soalnya tiap anime/source bisa punya kombinasi server beda-beda.
 *
 * Cara kerja matching: `server.name` (label dari API, mis. "Mp4Upload", "Server 1 - Filedon")
 * dicek apakah MENGANDUNG keyword pilihan user (case-insensitive). Kalau ketemu,
 * itu yang dipilih otomatis pas buka episode; kalau gak ketemu server dengan keyword
 * itu di episode tsb, fallback ke server pertama di list (behavior lama).
 */

class PreferencesManager(private val context: Context) {

    private val PREFERRED_SERVER_KEY = stringPreferencesKey("preferred_server_keyword")
    private val AUTOPLAY_NEXT_KEY = booleanPreferencesKey("autoplay_next_episode")
    private val DEFAULT_SOURCE_KEY = stringPreferencesKey("default_source")

    val preferredServerKeyword: Flow<String> = context.settingsDataStore.data
        .map { prefs -> prefs[PREFERRED_SERVER_KEY] ?: "" }

    suspend fun setPreferredServer(keyword: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[PREFERRED_SERVER_KEY] = keyword
        }
    }

    /**
     * Autoplay episode berikutnya begitu episode saat ini selesai diputar.
     * Default ON, karena ini yang lebih sering diharapkan user aplikasi streaming.
     */
    val autoplayNextEpisode: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[AUTOPLAY_NEXT_KEY] ?: true }

    suspend fun setAutoplayNextEpisode(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[AUTOPLAY_NEXT_KEY] = enabled
        }
    }

    /**
     * Sumber default (animasu/samehadaku/animekompi/donghua) yang otomatis
     * dipilih tiap kali app dibuka, biar user gak perlu milih ulang tiap sesi.
     * Kosong = belum pernah diset -> caller fallback ke sumber pertama (behavior lama).
     */
    val defaultSource: Flow<String> = context.settingsDataStore.data
        .map { prefs -> prefs[DEFAULT_SOURCE_KEY] ?: "" }

    suspend fun setDefaultSource(source: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[DEFAULT_SOURCE_KEY] = source
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

    // ==================== Auth session (Supabase) ====================
    // Disimpan lokal biar user tetap login walau app ditutup/HP di-restart.
    private val AUTH_ACCESS_TOKEN_KEY = stringPreferencesKey("auth_access_token")
    private val AUTH_REFRESH_TOKEN_KEY = stringPreferencesKey("auth_refresh_token")
    private val AUTH_USER_ID_KEY = stringPreferencesKey("auth_user_id")
    private val AUTH_USER_EMAIL_KEY = stringPreferencesKey("auth_user_email")
    private val AUTH_USER_NAME_KEY = stringPreferencesKey("auth_user_name")
    private val AUTH_USER_AVATAR_KEY = stringPreferencesKey("auth_user_avatar")

    data class AuthSession(
        val accessToken: String,
        val refreshToken: String,
        val userId: String,
        val email: String?,
        val name: String?,
        val avatarUrl: String?
    )

    val authSession: Flow<AuthSession?> = context.settingsDataStore.data
        .map { prefs ->
            val token = prefs[AUTH_ACCESS_TOKEN_KEY]
            val refresh = prefs[AUTH_REFRESH_TOKEN_KEY]
            val userId = prefs[AUTH_USER_ID_KEY]
            if (token.isNullOrBlank() || refresh.isNullOrBlank() || userId.isNullOrBlank()) {
                null
            } else {
                AuthSession(
                    accessToken = token,
                    refreshToken = refresh,
                    userId = userId,
                    email = prefs[AUTH_USER_EMAIL_KEY],
                    name = prefs[AUTH_USER_NAME_KEY],
                    avatarUrl = prefs[AUTH_USER_AVATAR_KEY]
                )
            }
        }

    suspend fun saveAuthSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        email: String?,
        name: String?,
        avatarUrl: String?
    ) {
        context.settingsDataStore.edit { prefs ->
            prefs[AUTH_ACCESS_TOKEN_KEY] = accessToken
            prefs[AUTH_REFRESH_TOKEN_KEY] = refreshToken
            prefs[AUTH_USER_ID_KEY] = userId
            if (email != null) prefs[AUTH_USER_EMAIL_KEY] = email
            if (name != null) prefs[AUTH_USER_NAME_KEY] = name
            if (avatarUrl != null) prefs[AUTH_USER_AVATAR_KEY] = avatarUrl
        }
    }

    /** Update nama/foto profil doang, tanpa ngutak-atik token yang lagi tersimpan. */
    suspend fun updateAuthProfileLocal(name: String?, avatarUrl: String?) {
        context.settingsDataStore.edit { prefs ->
            if (name != null) prefs[AUTH_USER_NAME_KEY] = name
            if (avatarUrl != null) prefs[AUTH_USER_AVATAR_KEY] = avatarUrl
        }
    }

    suspend fun clearAuthSession() {
        context.settingsDataStore.edit { prefs ->
            prefs.remove(AUTH_ACCESS_TOKEN_KEY)
            prefs.remove(AUTH_REFRESH_TOKEN_KEY)
            prefs.remove(AUTH_USER_ID_KEY)
            prefs.remove(AUTH_USER_EMAIL_KEY)
            prefs.remove(AUTH_USER_NAME_KEY)
            prefs.remove(AUTH_USER_AVATAR_KEY)
        }
    }

    // ==================== Onboarding (intro pertama install) ====================
    private val HAS_SEEN_ONBOARDING_KEY = booleanPreferencesKey("has_seen_onboarding")

    val hasSeenOnboarding: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[HAS_SEEN_ONBOARDING_KEY] ?: false }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[HAS_SEEN_ONBOARDING_KEY] = seen
        }
    }
}
