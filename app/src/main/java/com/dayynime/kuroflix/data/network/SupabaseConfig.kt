package com.dayynime.kuroflix.data.network

/**
 * Konfigurasi Supabase self-hosted (VPS Rumahweb).
 *
 * SUPABASE_URL: base URL server Supabase (Kong gateway), harus diakhiri "/".
 * SUPABASE_ANON_KEY: ANON_KEY dari .env server (aman dipakai di client,
 *   perlindungan data tetap lewat Row Level Security di Postgres).
 * GOOGLE_WEB_CLIENT_ID: HARUS SAMA PERSIS dengan GOOGLE_CLIENT_ID yang
 *   dikonfigurasi di .env server -- GoTrue nge-validasi audience id_token
 *   Google terhadap client ID ini.
 */
object SupabaseConfig {
    const val SUPABASE_URL = "http://203-175-11-166.nip.io:8000/"

    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImlzcyI6InN1cGFiYXNlIiwiaWF0IjoxNzUzMDIwODAwLCJleHAiOjIwNjg2MDA4MDB9.mWhbHcIwiTOWXfTTD-sFn-ykfof1vf9dj2VvwO3NCg8"

    const val GOOGLE_WEB_CLIENT_ID = "592000451797-8tq0be87569l9m2pll8b3jtv2k7ci4m5.apps.googleusercontent.com"
}
