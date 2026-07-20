package com.dayynime.kuroflix.data.network

/**
 * Config Cloudinary buat upload foto profil.
 *
 * SENGAJA cuma pakai CLOUD_NAME + UPLOAD_PRESET (unsigned), BUKAN API_SECRET.
 * API_SECRET gak boleh ditaruh di kode app: APK bisa di-decompile, dan siapa
 * aja yang nemu secret itu bisa hapus/rusak semua file di akun Cloudinary lo
 * (bukan cuma upload). Unsigned upload preset aman buat dipakai di client
 * karena cuma bisa dipakai buat upload sesuai aturan yang lo set di preset
 * (misal batas ukuran file), dan gak bisa dipakai buat hapus/timpa file lain.
 *
 * Cara bikin preset unsigned: Cloudinary Console -> Settings -> Upload ->
 * Upload presets -> Add upload preset -> Signing Mode: Unsigned -> Save.
 */
object CloudinaryConfig {
    const val CLOUD_NAME = "dzfkklsza"

    const val UPLOAD_PRESET = "kuroflix_avatar"

    const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
}
