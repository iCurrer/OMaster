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

    suspend fun fetchAndSave(context: Context, url: String, forceUpdate: Boolean = false): Result<PresetList> {
        Log.d("PresetRemoteManager", "Starting fetch from $url")
        return try {
            val response: HttpResponse = client.get(url)
            val text: String = response.body()
            
            // 验证 JSON 是否有效
            val presetList = try {
                Json.decodeFromString(PresetList.serializer(), text)
            } catch (e: Exception) {
                Log.e("PresetRemoteManager", "Invalid JSON received", e)
                return Result.failure(Exception("JSON 格式错误"))
            }

            // 验证必填字段
            val missingFields = mutableListOf<String>()
            if (presetList.name.isNullOrBlank()) missingFields.add("name (订阅名称)")
            if (presetList.author.isNullOrBlank()) missingFields.add("author (作者)")            
            if (missingFields.isNotEmpty()) {
                val errorMsg = "缺少必要字段: ${missingFields.joinToString(", ")}"
                return Result.failure(Exception(errorMsg))
            }

            val subManager = com.silas.omaster.data.local.SubscriptionManager.getInstance(context)
            
            // 检查版本号是否相同
            if (!forceUpdate) {
                val currentSub = subManager.subscriptionsFlow.value.find { it.url == url }
                if (currentSub != null && currentSub.build == presetList.build) {
                    return Result.failure(Exception("无需更新"))
                }
            }

            withContext(Dispatchers.IO) {
                val fileName = subManager.getFileNameForUrl(url)
                val file = File(context.filesDir, fileName)
                file.writeText(text)
                Log.d("PresetRemoteManager", "Saved remote presets to ${file.absolutePath}")
                
                // Update subscription info
                subManager.updateSubscriptionStatus(
                    url = url,
                    presetCount = presetList.presets.size,
                    lastUpdateTime = System.currentTimeMillis(),
                    name = presetList.name,
                    author = presetList.author,
                    build = presetList.build
                )

                // Invalidate JsonUtil cache so subsequent loads read the new remote file
                try {
                    JsonUtil.invalidateCache()
                } catch (e: Exception) {
                    Log.w("PresetRemoteManager", "Failed to invalidate JsonUtil cache", e)
                }
            }
            Result.success(presetList)
        } catch (e: Exception) {
            Log.e("PresetRemoteManager", "Failed to save presets", e)
            Result.failure(e)
        }
    }
}
