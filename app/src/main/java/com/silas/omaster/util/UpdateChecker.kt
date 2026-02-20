package com.silas.omaster.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.silas.omaster.R

/**
 * GitHub 更新检查工具
 * 从 GitHub Releases API 获取最新版本信息
 * 
 * 【国内下载方案】
 * 在 GitHub Release 的 body 中按以下格式填写国内下载链接：
 * 
 * ## 更新内容
 * - 新增 xxx 功能
 * - 修复 xxx 问题
 * 
 * ## 下载地址
 * - 国内下载：https://www.pgyer.com/omaster
 * - 备用下载：https://wwp.lanzoup.com/xxxx
 * 
 * 支持的关键字：国内下载、备用下载、蒲公英、蓝奏云
 */
object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/iCurrer/OMaster/releases/latest"
    private const val GITHUB_RELEASE_URL = "https://github.com/iCurrer/OMaster/releases/latest"

    /**
     * 更新信息数据类
     * 
     * @param versionName 版本名称
     * @param versionCode 版本号
     * @param downloadUrl 国内下载链接（优先）或 GitHub 链接
     * @param releaseNotes 更新日志（已清理下载链接部分）
     * @param isNewer 是否为新版本
     */
    data class UpdateInfo(
        val versionName: String,
        val versionCode: Int,
        val downloadUrl: String,
        val releaseNotes: String,
        val isNewer: Boolean
    )

    /**
     * 检查更新
     * @param currentVersionCode 当前版本号
     * @return 版本信息，如果失败返回 null
     */
    suspend fun checkUpdate(context: Context, currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                // 解析版本信息
                val tagName = json.getString("tag_name") // 例如 "v1.1.0"
                val versionName = tagName.removePrefix("v")

                // 从 tag_name 解析 versionCode (1.1.0 -> 10100)
                val versionCode = VersionInfo.parseVersionCode(versionName)

                // 获取 GitHub 下载链接（作为备用）
                val assets = json.getJSONArray("assets")
                var githubDownloadUrl = GITHUB_RELEASE_URL
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.getString("name")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        githubDownloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                // 获取原始更新日志
                val rawReleaseNotes = json.optString("body", context.getString(R.string.no_release_notes))
                
                // 【新增】解析国内下载链接
                val domesticUrl = extractDomesticUrl(rawReleaseNotes)
                
                // 【新增】清理更新日志，移除下载链接部分
                val cleanReleaseNotes = cleanReleaseNotes(rawReleaseNotes)
                
                // 优先使用国内链接，如果没有则使用 GitHub 链接
                val finalDownloadUrl = domesticUrl ?: githubDownloadUrl
                
                if (domesticUrl != null) {
                    Log.d(TAG, "使用国内下载链接: $domesticUrl")
                } else {
                    Log.d(TAG, "未找到国内下载链接，使用 GitHub 链接: $githubDownloadUrl")
                }

                // 判断是否为新版本
                val isNewer = versionCode > currentVersionCode

                UpdateInfo(
                    versionName = versionName,
                    versionCode = versionCode,
                    downloadUrl = finalDownloadUrl,
                    releaseNotes = cleanReleaseNotes,
                    isNewer = isNewer
                )
            } else {
                Log.e(TAG, "检查更新失败，HTTP 状态码: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "检查更新出错", e)
            null
        }
    }

    /**
     * 【新增】从 Release Notes 中提取国内下载链接
     * 
     * 支持的关键字（按优先级排序）：
     * 1. 国内下载
     * 2. 备用下载
     * 3. 蒲公英
     * 4. 蓝奏云
     * 
     * @param body GitHub Release 的 body 内容
     * @return 国内下载链接，如果没有找到返回 null
     */
    private fun extractDomesticUrl(body: String): String? {
        val patterns = listOf(
            // 匹配 "国内下载" 关键字
            Regex("国内下载[：:]\\s*(https?://[^\\s\\n]+)", RegexOption.IGNORE_CASE),
            // 匹配 "备用下载" 关键字
            Regex("备用下载[：:]\\s*(https?://[^\\s\\n]+)", RegexOption.IGNORE_CASE),
            // 匹配 "蒲公英" 关键字
            Regex("蒲公英[：:]\\s*(https?://[^\\s\\n]+)", RegexOption.IGNORE_CASE),
            // 匹配 "蓝奏云" 关键字
            Regex("蓝奏云[：:]\\s*(https?://[^\\s\\n]+)", RegexOption.IGNORE_CASE),
            // 匹配 "国内镜像" 关键字
            Regex("国内镜像[：:]\\s*(https?://[^\\s\\n]+)", RegexOption.IGNORE_CASE),
            // 匹配 "下载地址" 后面的链接
            Regex("下载地址[：:]\\s*(https?://[^\\s\\n]+)", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in patterns) {
            pattern.find(body)?.groupValues?.get(1)?.let { url ->
                Log.d(TAG, "找到国内下载链接: $url (匹配模式: ${pattern.pattern})")
                return url
            }
        }
        
        return null
    }

    /**
     * 【新增】清理更新日志，移除下载链接部分
     * 
     * 保留 "## 下载地址" 或 "国内下载" 之前的所有内容
     * 让更新日志更干净，不包含下载链接
     * 
     * @param body 原始 Release Notes
     * @return 清理后的更新日志
     */
    private fun cleanReleaseNotes(body: String): String {
        // 按优先级查找需要截断的位置
        val cutPoints = listOf(
            "## 下载地址",
            "## 国内下载",
            "## 备用下载",
            "国内下载：",
            "国内下载:",
            "备用下载：",
            "备用下载:",
            "下载地址：",
            "下载地址:"
        )
        
        var result = body
        for (cutPoint in cutPoints) {
            val index = result.indexOf(cutPoint, ignoreCase = true)
            if (index != -1) {
                result = result.substring(0, index).trim()
                Log.d(TAG, "清理更新日志，截断点: $cutPoint")
                break
            }
        }
        
        // 如果清理后为空，返回原始内容
        return if (result.isBlank()) body.trim() else result
    }

    /**
     * 打开浏览器下载页面
     * @param context 上下文
     * @param url 下载链接（优先使用国内链接）
     */
    fun openDownloadPage(context: Context, url: String = GITHUB_RELEASE_URL) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
