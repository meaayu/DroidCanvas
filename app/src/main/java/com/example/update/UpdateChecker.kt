package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.BuildConfig

data class UpdateInfo(val versionName: String, val apkUrl: String)

object UpdateChecker {
    private const val TAG = "UpdateChecker"
    private const val GITHUB_RELEASE_URL = "https://api.github.com/repos/meaayu/refcanvas/releases/latest"
    private const val PREFS_NAME = "update_checker_prefs"
    private const val KEY_LAST_CHECK = "last_check_time"
    private const val COOLDOWN_MS = 24 * 60 * 60 * 1000L // 24 hours

    private var downloadJob: kotlinx.coroutines.Job? = null
    private var activeConnection: HttpURLConnection? = null

    fun cancelDownload() {
        Log.d(TAG, "Cancelling active download")
        downloadJob?.cancel()
        downloadJob = null
        val conn = activeConnection
        activeConnection = null
        if (conn != null) {
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                try {
                    conn.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "Error disconnecting download connection", e)
                }
            }
        }
    }

    suspend fun checkForUpdates(context: Context, currentVersion: String = BuildConfig.VERSION_NAME, force: Boolean = false): UpdateInfo? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0L)
        val now = System.currentTimeMillis()
        if (!force && (now - lastCheck) < COOLDOWN_MS) {
            Log.d(TAG, "Update check skipped due to cooldown. Last check: $lastCheck, current time: $now")
            return@withContext null
        }
        prefs.edit().putLong(KEY_LAST_CHECK, now).apply()

        // 1. PRIMARY PATH: Check using rate-limit-resistant HTML redirects of /releases/latest.
        // Standard github.com doesn't enforce the aggressive 60 reqs/hr REST API rate limits.
        var redirectConnection: HttpURLConnection? = null
        try {
            val redirectUrl = URL("https://github.com/meaayu/refcanvas/releases/latest")
            redirectConnection = redirectUrl.openConnection() as HttpURLConnection
            redirectConnection.apply {
                instanceFollowRedirects = false
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            }
            val code = redirectConnection.responseCode
            if (code in 300..399) {
                val location = redirectConnection.getHeaderField("Location")
                if (location != null && location.isNotEmpty()) {
                    val tagName = when {
                        location.contains("/tag/") -> location.substringAfter("/tag/").substringBefore("?")
                        location.contains("/releases/") -> location.substringAfterLast("/").substringBefore("?")
                        else -> ""
                    }.trim()
                    
                    if (tagName.isNotEmpty()) {
                        Log.d(TAG, "Successfully determined latest release tag via redirect: $tagName")
                        val apkUrl = "https://github.com/meaayu/refcanvas/releases/download/$tagName/app-debug.apk"
                        if (isNewerVersion(currentVersion, tagName)) {
                            return@withContext UpdateInfo(versionName = tagName, apkUrl = apkUrl)
                        } else {
                            return@withContext null
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Primary redirect update check failed, attempting JSON API fallback", e)
        } finally {
            redirectConnection?.disconnect()
        }

        // 2. FALLBACK PATH: Standard JSON API
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GITHUB_RELEASE_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "RefCanvas-App")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val tagName = jsonResponse.optString("tag_name", "")
                if (tagName.isEmpty()) return@withContext null

                val assets = jsonResponse.optJSONArray("assets")
                var apkUrl: String? = null
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                if (apkUrl != null && apkUrl.isNotEmpty()) {
                    if (isNewerVersion(currentVersion, tagName)) {
                        return@withContext UpdateInfo(versionName = tagName, apkUrl = apkUrl)
                    }
                }
            } else {
                val errorText = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                } catch (e: Exception) {
                    "Could not read error stream: ${e.message}"
                }
                val rateLimitRemaining = connection.getHeaderField("X-RateLimit-Remaining")
                val rateLimitReset = connection.getHeaderField("X-RateLimit-Reset")
                Log.e(TAG, "Failed to check for updates: HTTP $responseCode. Error body: $errorText. Rate limit remaining: $rateLimitRemaining, reset: $rateLimitReset")
                if (responseCode == 403) {
                    throw java.io.IOException("GitHub API rate limit exceeded. Please try again later.")
                } else {
                    throw java.io.IOException("Failed to check for updates (HTTP $responseCode)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates via fallback path", e)
            throw e
        } finally {
            connection?.disconnect()
        }
        return@withContext null
    }

    fun isNewerVersion(current: String, release: String): Boolean {
        val curClean = current.trim().removePrefix("v")
        val relClean = release.trim().removePrefix("v")
        
        val curParts = curClean.split(".")
        val relParts = relClean.split(".")
        val maxLen = maxOf(curParts.size, relParts.size)
        
        for (i in 0 until maxLen) {
            val curPart = curParts.getOrNull(i) ?: ""
            val relPart = relParts.getOrNull(i) ?: ""
            val curVal = curPart.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
            val relVal = relPart.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
            
            if (relVal > curVal) return true
            if (relVal < curVal) return false
        }
        return false
    }

    private suspend fun resolveFinalUrl(startUrl: String, maxRedirects: Int = 5): String = withContext(Dispatchers.IO) {
        var currentUrl = startUrl
        repeat(maxRedirects) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "RefCanvas-App")
            val code = connection.responseCode
            val location = connection.getHeaderField("Location")
            connection.disconnect()
            if (code in 300..399 && location != null) {
                currentUrl = location
            } else {
                return@withContext currentUrl
            }
        }
        return@withContext currentUrl
    }

    fun downloadAndInstall(
        context: Context,
        apkUrl: String,
        versionName: String,
        onProgress: ((Float) -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        val coroutineScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
        downloadJob = coroutineScope.launch {
            var connection: HttpURLConnection? = null
            try {
                val resolvedUrl = try {
                    resolveFinalUrl(apkUrl)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to resolve redirect, falling back to original URL", e)
                    apkUrl
                }
                Log.d(TAG, "Resolved download URL: $resolvedUrl")

                val targetDir = File(context.getExternalFilesDir(null), "downloads")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val targetFile = File(targetDir, "refcanvas-$versionName.apk")
                if (targetFile.exists()) {
                    targetFile.delete()
                }

                connection = URL(resolvedUrl).openConnection() as HttpURLConnection
                activeConnection = connection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("User-Agent", "RefCanvas-App")
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw java.io.IOException("Server returned HTTP $responseCode while downloading APK")
                }

                val totalBytes = connection.contentLength.toLong()
                var downloadedBytes = 0L
                var lastReportedProgress = -1

                connection.inputStream.use { input ->
                    java.io.FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        while (this.isActive) {
                            val bytesRead = input.read(buffer)
                            if (bytesRead == -1) break
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                val progress = downloadedBytes.toFloat() / totalBytes
                                val progressPercent = (progress * 100).toInt()
                                if (progressPercent != lastReportedProgress) {
                                    lastReportedProgress = progressPercent
                                    withContext(Dispatchers.Main) {
                                        onProgress?.invoke(progress)
                                    }
                                }
                            }
                        }
                        output.flush()
                    }
                }

                if (!this.isActive) {
                    Log.d(TAG, "Download job was cancelled during streaming")
                    return@launch
                }

                Log.d(TAG, "Download complete: ${targetFile.absolutePath}, size=${targetFile.length()} bytes")

                withContext(Dispatchers.Main) {
                    onProgress?.invoke(1f)
                    installApk(context, targetFile)
                    onSuccess?.invoke()
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Download coroutine cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download/install update", e)
                withContext(Dispatchers.Main) {
                    onFailure?.invoke(e.localizedMessage ?: "Unknown error during download")
                }
            } finally {
                connection?.disconnect()
                if (activeConnection == connection) {
                    activeConnection = null
                }
            }
        }
    }

    private fun installApk(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
            
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch APK installation", e)
        }
    }
}
