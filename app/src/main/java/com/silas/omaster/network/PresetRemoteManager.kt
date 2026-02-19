package com.silas.omaster.network

import android.content.Context
import android.util.Log
import com.silas.omaster.model.PresetList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.silas.omaster.util.JsonUtil
import kotlinx.serialization.json.Json
import java.io.File

object PresetRemoteManager {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchPresets(url: String): PresetList? {
        Log.d("PresetRemoteManager", "Starting fetch from $url")
        return try {
            val response: HttpResponse = client.get(url)
            // Some servers (GitHub raw) may return Content-Type: text/plain; charset=utf-8
            // which prevents Ktor's content-negotiation from selecting the JSON transformer.
            // Read as text and decode explicitly to avoid NoTransformationFoundException.
            val text: String = response.body()
            val presets = Json.decodeFromString(PresetList.serializer(), text)
            Log.d("PresetRemoteManager", "Fetched ${presets.presets.size} presets")
            presets
        } catch (e: Exception) {
            Log.e("PresetRemoteManager", "Failed to fetch presets", e)
            null
        }
    }

    suspend fun fetchAndSave(context: Context, url: String): Boolean {
        val presets = fetchPresets(url) ?: return false
        return try {
            withContext(Dispatchers.IO) {
                val file = File(context.filesDir, "presets_remote.json")
                val jsonText = Json.encodeToString(PresetList.serializer(), presets)
                file.writeText(jsonText)
                Log.d("PresetRemoteManager", "Saved remote presets to ${file.absolutePath}")
                // Invalidate JsonUtil cache so subsequent loads read the new remote file
                try {
                    JsonUtil.invalidateCache()
                } catch (e: Exception) {
                    Log.w("PresetRemoteManager", "Failed to invalidate JsonUtil cache", e)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("PresetRemoteManager", "Failed to save presets", e)
            false
        }
    }
}
