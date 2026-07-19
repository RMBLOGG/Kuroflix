package com.dayynime.kuroflix.data.network

/**
 * Konfigurasi Supabase self-hosted (VPS Rumahweb).
 *
 * SUPABASE_URL: base URL server Supabase (Kong gateway), harus diakhiri "/".
 * SUPABASE_ANON_KEY: publishable API key (format sb_publishable_...) dari
 *   .env server (variabel SUPABASE_PUBLISHABLE_KEY). Kong di versi Supabase
 *   ini validasi header apikey terhadap key format baru ini, BUKAN lagi
 *   ANON_KEY versi lama (legacy JWT) -- pakai ANON_KEY lama bakal 401.
 *   perlindungan data tetap lewat Row Level Security di Postgres).
 * GOOGLE_WEB_CLIENT_ID: HARUS SAMA PERSIS dengan GOOGLE_CLIENT_ID yang
 *   dikonfigurasi di .env server -- GoTrue nge-validasi audience id_token
 *   Google terhadap client ID ini.
 */
object SupabaseConfig {
    const val SUPABASE_URL = "http://203-175-11-166.nip.io:8000/"

    const val SUPABASE_ANON_KEY = "sb_publishable_Q-mFUwagUI7cE4k-OQYQPf_bc1QS87l"

    const val GOOGLE_WEB_CLIENT_ID = "592000451797-8tq0be87569l9m2pll8b3jtv2k7ci4m5.apps.googleusercontent.com"
}
