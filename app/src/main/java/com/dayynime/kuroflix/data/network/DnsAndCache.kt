package com.dayynime.kuroflix.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import okhttp3.*
import okhttp3.Dns
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class AppDns : Dns {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    override fun lookup(hostname: String): List<InetAddress> {
        try {
            // First try standard system resolution
            return Dns.SYSTEM.lookup(hostname)
        } catch (e: UnknownHostException) {
            Log.w("AppDns", "System DNS failed for $hostname, trying DNS-over-HTTPS...")
            try {
                return resolveWithDoH(hostname)
            } catch (ex: Exception) {
                Log.e("AppDns", "DoH failed for $hostname", ex)
                throw UnknownHostException("Could not resolve host $hostname via standard or DoH: ${ex.message}")
            }
        }
    }

    private fun resolveWithDoH(hostname: String): List<InetAddress> {
        val url = "https://dns.google/resolve?name=$hostname&type=A"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val bodyString = response.body?.string() ?: throw IOException("Empty body")
            
            val json = JSONObject(bodyString)
            val answerArray = json.optJSONArray("Answer") ?: return emptyList()
            val addresses = mutableListOf<InetAddress>()
            for (i in 0 until answerArray.length()) {
                val answer = answerArray.getJSONObject(i)
                val type = answer.optInt("type")
                val data = answer.optString("data")
                if (type == 1 && data.isNotEmpty()) { // Type 1 is A record
                    try {
                        addresses.add(InetAddress.getByName(data))
                    } catch (e: Exception) {
                        // ignore invalid IP
                    }
                }
            }
            if (addresses.isEmpty()) {
                throw UnknownHostException("No valid IP returned from DoH for $hostname")
            }
            return addresses
        }
    }
}

class AnimasuApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url
        if (url.encodedPath.contains("/animasu/")) {
            val newUrl = url.newBuilder()
                .addQueryParameter("apikey", "planaai")
                .build()
            request = request.newBuilder().url(newUrl).build()
        }
        return chain.proceed(request)
    }
}

class CacheInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!isNetworkAvailable(context)) {
            // Cache control offline: up to 7 days
            request = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7)
                .build()
        }
        val response = chain.proceed(request)
        if (isNetworkAvailable(context)) {
            // Cache control online: cache for 1 hour
            return response.newBuilder()
                .header("Cache-Control", "public, max-age=" + 60 * 60)
                .removeHeader("Pragma")
                .build()
        }
        return response
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        )
    }
}

fun createCachedOkHttpClient(context: Context): OkHttpClient {
    val cacheFile = File(context.cacheDir, "http_cache")
    val cache = Cache(cacheFile, 50 * 1024 * 1024) // 50MB
    return OkHttpClient.Builder()
        .cache(cache)
        .dns(AppDns())
        .addInterceptor(AnimasuApiKeyInterceptor())
        .addInterceptor(CacheInterceptor(context))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
}
