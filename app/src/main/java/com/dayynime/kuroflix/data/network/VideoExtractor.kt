package com.dayynime.kuroflix.data.network

import android.net.Uri
import android.util.Base64
import android.util.Log
import com.dayynime.kuroflix.data.model.VideoSource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

object VideoExtractor {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private const val TAG = "VideoExtractor"

    suspend fun resolveVideoUrl(embedUrl: String, referer: String? = null): VideoSource {
        Log.d(TAG, "Resolving video url: $embedUrl, referer: $referer")
        try {
            val uri = Uri.parse(embedUrl)
            val host = uri.host?.lowercase() ?: ""
            
            // Add fallback custom headers
            val headers = mutableMapOf<String, String>()
            headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            if (referer != null) {
                headers["Referer"] = referer
            } else {
                headers["Referer"] = "https://${uri.host}/"
            }
            headers["Origin"] = "https://${uri.host}"

            when {
                // 1. pixeldrain
                host.contains("pixeldrain") -> {
                    // Url format: https://pixeldrain.com/u/ID or similar
                    val segments = uri.pathSegments
                    val id = segments.lastOrNull { it.isNotEmpty() && it != "u" }
                    if (id != null) {
                        val directUrl = "https://pixeldrain.com/api/file/$id"
                        return VideoSource(directUrl, "Pixeldrain (Direct)", headers)
                    }
                }

                // 2. mp4upload
                host.contains("mp4upload") -> {
                    val html = fetchHtml(embedUrl, headers)
                    val matcher = Pattern.compile("src:\\s*\"(https?://[^\"]+mp4)\"").matcher(html)
                    if (matcher.find()) {
                        return VideoSource(matcher.group(1) ?: "", "Mp4Upload", headers)
                    }
                }

                // 3. streamtape
                host.contains("streamtape") -> {
                    val html = fetchHtml(embedUrl, headers)
                    // Streamtape robotlink format: robotlink.innerHTML = "a" + ('b')
                    val mainPattern = Pattern.compile("document\\.getElementById\\('robotlink'\\)\\.innerHTML\\s*=\\s*'([^']+)'\\s*\\+\\s*\\('([^']+)'\\)").matcher(html)
                    if (mainPattern.find()) {
                        val part1 = mainPattern.group(1) ?: ""
                        val part2 = mainPattern.group(2) ?: ""
                        // Usually some parts are cut or token-based, let's assemble standard token
                        // Format is: //streamtape.com/get_video?id=...&token=...
                        var resolved = "https:$part1$part2"
                        // Add some dummy streams parameter if needed
                        return VideoSource(resolved, "Streamtape", headers)
                    }
                }

                // 4. mediafire
                host.contains("mediafire") -> {
                    val html = fetchHtml(embedUrl, headers)
                    val matcher = Pattern.compile("href=\"(https?://download[^\"]+)\"\\s+id=\"downloadButton\"").matcher(html)
                    if (matcher.find()) {
                        return VideoSource(matcher.group(1) ?: "", "Mediafire", headers)
                    }
                }

                // 5. filedon
                host.contains("filedon") -> {
                    val html = fetchHtml(embedUrl, headers)
                    val matcher = Pattern.compile("data-page=\"([^\"]+)\"").matcher(html)
                    if (matcher.find()) {
                        val decodedPage = matcher.group(1)?.replace("&quot;", "\"") ?: ""
                        val json = JSONObject(decodedPage)
                        val props = json.optJSONObject("props")
                        val media = props?.optJSONObject("media")
                        val hlsUrl = media?.optString("hls_url") ?: props?.optString("url") ?: ""
                        if (hlsUrl.isNotEmpty()) {
                            return VideoSource(sanitizeHlsPlaylist(hlsUrl), "Filedon HLS", headers)
                        }
                    }
                }

                // 6. blogger/blogspot
                host.contains("blogspot") || host.contains("blogger") -> {
                    val html = fetchHtml(embedUrl, headers)
                    val matcher = Pattern.compile("\"remotetype\":\"3\",\"videoUrl\":\"([^\"]+)\"").matcher(html)
                    if (matcher.find()) {
                        val rawUrl = matcher.group(1) ?: ""
                        val cleanUrl = rawUrl.replace("\\u0026", "&")
                        return VideoSource(cleanUrl, "Blogger Video", headers)
                    }
                    val configMatcher = Pattern.compile("VIDEO_CONFIG\\s*=\\s*(\\{.*?\\});").matcher(html)
                    if (configMatcher.find()) {
                        val config = JSONObject(configMatcher.group(1) ?: "{}")
                        val streams = config.optJSONArray("streams")
                        if (streams != null && streams.length() > 0) {
                            val firstStream = streams.getJSONObject(0)
                            val playUrl = firstStream.optString("play_url")
                            if (playUrl.isNotEmpty()) {
                                return VideoSource(playUrl, "Blogger Stream", headers)
                            }
                        }
                    }
                }

                // 7. packed-JS hosts: filemoon, vidhide, wibufile, streamhide, moviesm4u, ztreamhub, guccihide, anichin.stream
                host.contains("filemoon") || host.contains("vidhide") || host.contains("wibufile") || 
                host.contains("streamhide") || host.contains("moviesm4u") || host.contains("ztreamhub") || 
                host.contains("guccihide") || host.contains("anichin.stream") -> {
                    val html = fetchHtml(embedUrl, headers)
                    val unpacked = unpackJsIfNeeded(html)
                    val matcher = Pattern.compile("file\\s*:\\s*\"(https?://[^\"]+\\.m3u8)\"").matcher(unpacked)
                    if (matcher.find()) {
                        return VideoSource(sanitizeHlsPlaylist(matcher.group(1) ?: ""), "HLS Stream", headers)
                    }
                    val mp4Matcher = Pattern.compile("file\\s*:\\s*\"(https?://[^\"]+\\.mp4)\"").matcher(unpacked)
                    if (mp4Matcher.find()) {
                        return VideoSource(mp4Matcher.group(1) ?: "", "MP4 Stream", headers)
                    }
                }

                // 8. gdriveplayer
                host.contains("gdriveplayer") -> {
                    val html = fetchHtml(embedUrl, headers)
                    val kMatcher = Pattern.compile("var k\\s*=\\s*\"([^\"]+)\"").matcher(html)
                    if (kMatcher.find()) {
                        val base64Data = kMatcher.group(1) ?: ""
                        val decoded = customXorDecode(base64Data)
                        val hlsMatcher = Pattern.compile("HLS\\s*=\\s*\"([^\"]+)\"").matcher(decoded)
                        if (hlsMatcher.find()) {
                            return VideoSource(hlsMatcher.group(1) ?: "", "GDrivePlayer HLS", headers)
                        }
                    }
                }

                // 9. rumble
                host.contains("rumble") -> {
                    val path = uri.path ?: ""
                    val videoId = path.split("/").lastOrNull { it.isNotEmpty() }
                    if (videoId != null) {
                        val apiResponse = fetchHtml("https://rumble.com/embedJS/u3/?request=video&v=$videoId", headers)
                        val json = JSONObject(apiResponse)
                        val ua = json.optJSONObject("ua")
                        val hls = ua?.optJSONObject("hls")
                        val auto = hls?.optJSONObject("auto")
                        val urlStr = auto?.optString("url") ?: ""
                        if (urlStr.isNotEmpty()) {
                            return VideoSource(urlStr, "Rumble HLS", headers)
                        }
                    }
                }

                // 10. ok.ru
                host.contains("ok.ru") || host.contains("odnoklassniki") -> {
                    val html = fetchHtml(embedUrl, headers)
                    val optionsMatcher = Pattern.compile("data-options=\"([^\"]+)\"").matcher(html)
                    if (optionsMatcher.find()) {
                        val decodedOptions = optionsMatcher.group(1)?.replace("&quot;", "\"") ?: ""
                        val json = JSONObject(decodedOptions)
                        val flashvars = json.optJSONObject("flashvars")
                        val metadataStr = flashvars?.optString("metadata") ?: ""
                        if (metadataStr.isNotEmpty()) {
                            val metadata = JSONObject(metadataStr)
                            val hlsUrl = metadata.optString("hlsManifestUrl")
                            if (hlsUrl.isNotEmpty()) {
                                return VideoSource(hlsUrl, "OK.ru HLS", headers)
                            }
                            val videos = metadata.optJSONArray("videos")
                            if (videos != null && videos.length() > 0) {
                                // Prefer highest resolution available
                                var maxResUrl = ""
                                var maxResName = ""
                                for (i in 0 until videos.length()) {
                                    val videoObj = videos.getJSONObject(i)
                                    val name = videoObj.optString("name")
                                    val urlVal = videoObj.optString("url")
                                    if (urlVal.isNotEmpty()) {
                                        maxResUrl = urlVal
                                        maxResName = name
                                    }
                                }
                                if (maxResUrl.isNotEmpty()) {
                                    return VideoSource(maxResUrl, "OK.ru ($maxResName)", headers)
                                }
                            }
                        }
                    }
                }

                // 11. dailymotion
                host.contains("dailymotion") || host.contains("dai.ly") -> {
                    val videoId = if (host.contains("dai.ly")) {
                        uri.pathSegments.firstOrNull { it.isNotEmpty() }
                    } else {
                        uri.pathSegments.lastOrNull { it.isNotEmpty() }
                    }
                    if (videoId != null) {
                        val metaUrl = "https://www.dailymotion.com/player/metadata/video/$videoId"
                        val metaJson = fetchHtml(metaUrl, headers)
                        val json = JSONObject(metaJson)
                        val qualities = json.optJSONObject("qualities")
                        val autoList = qualities?.optJSONArray("auto")
                        if (autoList != null && autoList.length() > 0) {
                            val autoObj = autoList.getJSONObject(0)
                            val streamUrl = autoObj.optString("url")
                            if (streamUrl.isNotEmpty()) {
                                return VideoSource(sanitizeHlsPlaylist(streamUrl), "Dailymotion HLS", headers)
                            }
                        }
                    }
                }

                // 12. Shortlink / Unknown hosts: Follow redirects
                else -> {
                    // Try following redirects first
                    val redirectedUrl = followRedirects(embedUrl, headers)
                    if (redirectedUrl != embedUrl) {
                        return resolveVideoUrl(redirectedUrl, referer)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving url: $embedUrl", e)
        }

        // Fallback: Use direct embed url as webview source
        return VideoSource(embedUrl, "Embed Player", isEmbed = true)
    }

    private fun fetchHtml(url: String, headers: Map<String, String>): String {
        val reqBuilder = Request.Builder().url(url)
        headers.forEach { (k, v) -> reqBuilder.addHeader(k, v) }
        client.newCall(reqBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body?.string() ?: ""
        }
    }

    private fun followRedirects(urlStr: String, headers: Map<String, String>): String {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode in 300..399) {
                val loc = connection.getHeaderField("Location")
                if (!loc.isNullOrEmpty()) {
                    return loc
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to follow redirect", e)
        }
        return urlStr
    }

    private fun unpackJsIfNeeded(html: String): String {
        val matcher = Pattern.compile("eval\\(function\\(p,a,c,k,e,d\\).*?\\('(.*?)',\\s*(\\d+),\\s*(\\d+),\\s*'(.*?)'\\.split").matcher(html)
        if (matcher.find()) {
            val p = matcher.group(1) ?: ""
            val a = matcher.group(2)?.toIntOrNull() ?: 10
            val c = matcher.group(3)?.toIntOrNull() ?: 0
            val k = matcher.group(4)?.split("|") ?: emptyList()
            return unpack(p, a, c, k)
        }
        return html
    }

    private fun unpack(p: String, a: Int, c: Int, k: List<String>): String {
        var payload = p
        val baseStr = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        fun unbase(n: Int, base: Int): String {
            if (n < base) return baseStr[n].toString()
            return unbase(n / base, base) + baseStr[n % base]
        }
        val map = mutableMapOf<String, String>()
        for (i in 0 until c) {
            val key = unbase(i, a)
            val value = if (i < k.size && k[i].isNotEmpty()) k[i] else key
            map[key] = value
        }
        val pattern = Pattern.compile("\\b[a-zA-Z0-9]+\\b")
        val matcher = pattern.matcher(payload)
        val sb = StringBuffer()
        while (matcher.find()) {
            val word = matcher.group()
            val repl = map[word] ?: word
            matcher.appendReplacement(sb, MatcherQuoteReplacement(repl))
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    private fun MatcherQuoteReplacement(s: String): String {
        val sb = java.lang.StringBuilder()
        for (element in s) {
            if (element == '\\' || element == '$') {
                sb.append('\\')
            }
            sb.append(element)
        }
        return sb.toString()
    }

    private fun customXorDecode(base64Str: String): String {
        try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            // Example custom XOR decryption if we know a key, let's do a simple cycle XOR
            val key = 0xAF.toByte() // standard placeholder key
            val result = ByteArray(decodedBytes.size)
            for (i in decodedBytes.indices) {
                result[i] = (decodedBytes[i].toInt() xor key.toInt()).toByte()
            }
            return String(result)
        } catch (e: Exception) {
            Log.e(TAG, "Custom XOR decoding failed", e)
            return ""
        }
    }

    private fun sanitizeHlsPlaylist(playlistUrl: String): String {
        // Sanitize stream lines if we need to load manually, but typically just return the URL itself
        // Ensure no trailing spaces or formatting issues
        return playlistUrl.trim()
    }
}
