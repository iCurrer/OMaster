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
                val migratedSubscriptions = list.subscriptions.map { sub ->
                    // 迁移逻辑：如果订阅名称是“官方内置预设”但 URL 不是最新的，则更新它
                    if (sub.name == "官方内置预设" && sub.url != UpdateConfigManager.DEFAULT_PRESET_URL) {
                        updated = true
                        sub.copy(url = UpdateConfigManager.DEFAULT_PRESET_URL)
                    } else {
                        sub
                    }
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
            // First time, add the default subscription
            val defaultSub = Subscription(
                url = UpdateConfigManager.DEFAULT_PRESET_URL,
                name = "官方内置预设",
                author = "@OMaster",
                build = 1,
                isEnabled = true
            )
            _subscriptionsFlow.value = listOf(defaultSub)
            saveSubscriptions()
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
