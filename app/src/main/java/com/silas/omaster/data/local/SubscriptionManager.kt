package com.silas.omaster.data.local

import android.content.Context
import com.silas.omaster.model.Subscription
import com.silas.omaster.model.SubscriptionList
import com.silas.omaster.util.UpdateConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStreamReader

@Deprecated(
    "使用 ConfigCenter 替代",
    ReplaceWith("ConfigCenter.getInstance(context)")
)
class SubscriptionManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _subscriptionsFlow = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptionsFlow: StateFlow<List<Subscription>> = _subscriptionsFlow.asStateFlow()

    init {
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        val jsonStr = prefs.getString(KEY_SUBSCRIPTIONS, null)
        if (jsonStr != null) {
            try {
                val list = json.decodeFromString<SubscriptionList>(jsonStr)
                var updated = false
                var migratedSubscriptions = list.subscriptions.map { sub ->
                    // 迁移逻辑：如果订阅名称是“官方内置预设”但 URL 不是最新的，则更新它
                    if (sub.name == "官方内置预设" && sub.url != UpdateConfigManager.DEFAULT_PRESET_URL) {
                        updated = true
                        sub.copy(url = UpdateConfigManager.DEFAULT_PRESET_URL)
                    } else {
                        sub
                    }
                }
                // 迁移：检查是否需要添加 Realme 订阅（老用户升级）
                val hasRealmeSub = migratedSubscriptions.any { 
                    it.url == UpdateConfigManager.REALME_PRESET_URL 
                }
                if (!hasRealmeSub) {
                    val realmeSub = Subscription(
                        url = UpdateConfigManager.REALME_PRESET_URL,
                        name = "Realme GR预设",
                        author = "@OMaster",
                        build = 1,
                        isEnabled = false,  // 默认关闭
                        presetCount = 0,
                        lastUpdateTime = 0
                    )
                    migratedSubscriptions = migratedSubscriptions + realmeSub
                    updated = true
                    android.util.Log.d("SubscriptionManager", "Added Realme subscription for existing user")
                }
                
                _subscriptionsFlow.value = migratedSubscriptions
                if (updated) {
                    saveSubscriptions()
                    android.util.Log.d("SubscriptionManager", "Migrated official subscription to new URL")
                }
            } catch (e: Exception) {
                android.util.Log.e("SubscriptionManager", "Failed to decode subscriptions", e)
                _subscriptionsFlow.value = emptyList()
            }
        } else {
            // First time, add the default subscriptions
            // 与云端 presets.json 保持一致：version 3, build 4
            // 从 assets 读取预设数量
            val presetCount = loadPresetCountFromAssets()
            
            val oppoSub = Subscription(
                url = UpdateConfigManager.DEFAULT_PRESET_URL,
                name = "OPPO / 一加 大师预设",
                author = "@OMaster",
                build = 4,
                isEnabled = true,
                presetCount = presetCount,
                lastUpdateTime = System.currentTimeMillis()
            )
            
            val realmeSub = Subscription(
                url = UpdateConfigManager.REALME_PRESET_URL,
                name = "Realme GR预设",
                author = "@OMaster",
                build = 1,
                isEnabled = false,  // 默认关闭
                presetCount = 0,
                lastUpdateTime = 0
            )
            
            _subscriptionsFlow.value = listOf(oppoSub, realmeSub)
            saveSubscriptions()
            android.util.Log.d("SubscriptionManager", "Created default subscriptions for new user")
        }
    }

    /**
     * 从 assets/presets.json 读取预设数量
     */
    private fun loadPresetCountFromAssets(): Int {
        return try {
            appContext.assets.open("presets.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val presetListType = object : com.google.gson.reflect.TypeToken<com.silas.omaster.model.PresetList>() {}.type
                    val presetList: com.silas.omaster.model.PresetList? = 
                        com.google.gson.Gson().fromJson(reader, presetListType)
                    presetList?.presets?.size ?: 0
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SubscriptionManager", "Failed to load preset count from assets", e)
            0
        }
    }

    private fun saveSubscriptions() {
        val list = SubscriptionList(_subscriptionsFlow.value)
        val jsonStr = json.encodeToString(SubscriptionList.serializer(), list)
        prefs.edit().putString(KEY_SUBSCRIPTIONS, jsonStr).apply()
    }

    fun addSubscription(url: String, name: String = "", author: String = "", build: Int = 1) {
        if (_subscriptionsFlow.value.any { it.url == url }) return
        val newSub = Subscription(url = url, name = name, author = author, build = build)
        _subscriptionsFlow.value = _subscriptionsFlow.value + newSub
        saveSubscriptions()
    }

    fun removeSubscription(url: String) {
        _subscriptionsFlow.value = _subscriptionsFlow.value.filterNot { it.url == url }
        saveSubscriptions()
        // Delete corresponding file
        val fileName = getFileNameForUrl(url)
        val file = File(appContext.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    fun toggleSubscription(url: String) {
        _subscriptionsFlow.value = _subscriptionsFlow.value.map {
            if (it.url == url) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveSubscriptions()
    }

    fun updateSubscriptionStatus(url: String, presetCount: Int, lastUpdateTime: Long, name: String? = null, author: String? = null, build: Int? = null) {
        _subscriptionsFlow.value = _subscriptionsFlow.value.map {
            if (it.url == url) {
                it.copy(
                    presetCount = presetCount,
                    lastUpdateTime = lastUpdateTime,
                    name = name ?: it.name,
                    author = author ?: it.author,
                    build = build ?: it.build
                )
            } else it
        }
        saveSubscriptions()
    }

    fun updateSubscriptionUrl(oldUrl: String, newUrl: String) {
        if (oldUrl == newUrl) return
        _subscriptionsFlow.value = _subscriptionsFlow.value.map {
            if (it.url == oldUrl) it.copy(url = newUrl) else it
        }
        saveSubscriptions()
        
        // 删除旧文件，新文件将在下次更新时创建
        val oldFileName = getFileNameForUrl(oldUrl)
        val oldFile = File(appContext.filesDir, oldFileName)
        if (oldFile.exists()) {
            oldFile.delete()
        }
    }

    fun getFileNameForUrl(url: String): String {
        // Use a hash of the URL to create a unique filename
        val hash = url.hashCode().toString(16)
        return "sub_$hash.json"
    }

    companion object {
        private const val PREFS_NAME = "omaster_subscriptions"
        private const val KEY_SUBSCRIPTIONS = "subscriptions_list"

        @Volatile
        private var INSTANCE: SubscriptionManager? = null

        fun getInstance(context: Context): SubscriptionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SubscriptionManager(context).also { INSTANCE = it }
            }
        }
    }
}
