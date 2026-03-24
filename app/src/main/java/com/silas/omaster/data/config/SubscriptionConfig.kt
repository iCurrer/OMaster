package com.silas.omaster.data.config

import android.content.Context
import android.content.SharedPreferences
import com.silas.omaster.model.Subscription
import com.silas.omaster.model.SubscriptionList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 订阅配置管理（ConfigCenter 子模块）
 * 处理订阅列表的持久化和状态管理
 */
internal class SubscriptionConfig(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val json = Json { ignoreUnknownKeys = true }

    private val _subscriptionsFlow = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptionsFlow: StateFlow<List<Subscription>> = _subscriptionsFlow.asStateFlow()

    companion object {
        const val DEFAULT_PRESET_URL = "https://cdn.jsdelivr.net/gh/fengyec2/OMaster-Community@main/presets/v2/oppo.json"
        const val REALME_PRESET_URL = "https://cdn.jsdelivr.net/gh/fengyec2/OMaster-Community@main/presets/v2/realme.json"

        private const val PREFS_NAME = "omaster_config_subscriptions"
        private const val KEY_SUBSCRIPTIONS = "subscriptions_list"
    }

    init {
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        val jsonStr = prefs.getString(KEY_SUBSCRIPTIONS, null)
        if (jsonStr != null) {
            try {
                val list = json.decodeFromString<SubscriptionList>(jsonStr)
                _subscriptionsFlow.value = list.subscriptions
            } catch (e: Exception) {
                android.util.Log.e("SubscriptionConfig", "Failed to load subscriptions", e)
                _subscriptionsFlow.value = emptyList()
            }
        } else {
            createDefaultSubscriptions()
        }
    }

    private fun createDefaultSubscriptions() {
        val defaultSubs = listOf(
            Subscription(
                url = DEFAULT_PRESET_URL,
                name = "OPPO / 一加 大师预设",
                author = "@OMaster",
                build = 4,
                isEnabled = true,
                presetCount = 0,
                lastUpdateTime = System.currentTimeMillis()
            ),
            Subscription(
                url = REALME_PRESET_URL,
                name = "Realme GR预设",
                author = "@OMaster",
                build = 1,
                isEnabled = false,
                presetCount = 0,
                lastUpdateTime = 0
            )
        )
        _subscriptionsFlow.value = defaultSubs
        saveSubscriptions()
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
        val fileName = getFileNameForUrl(url)
        File(context.filesDir, fileName).delete()
    }

    fun updateSubscriptionUrl(oldUrl: String, newUrl: String) {
        val oldFileName = getFileNameForUrl(oldUrl)
        val newFileName = getFileNameForUrl(newUrl)
        val oldFile = File(context.filesDir, oldFileName)
        val newFile = File(context.filesDir, newFileName)
        if (oldFile.exists()) {
            oldFile.renameTo(newFile)
        }
        _subscriptionsFlow.value = _subscriptionsFlow.value.map {
            if (it.url == oldUrl) it.copy(url = newUrl) else it
        }
        saveSubscriptions()
    }

    fun toggleSubscription(url: String) {
        _subscriptionsFlow.value = _subscriptionsFlow.value.map {
            if (it.url == url) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveSubscriptions()
    }

    fun updateSubscriptionStatus(
        url: String,
        presetCount: Int,
        lastUpdateTime: Long,
        name: String? = null,
        author: String? = null,
        build: Int? = null
    ) {
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
        val hash = url.hashCode().toString(16)
        return "sub_$hash.json"
    }

    private fun saveSubscriptions() {
        val list = SubscriptionList(_subscriptionsFlow.value)
        val jsonStr = json.encodeToString(SubscriptionList.serializer(), list)
        prefs.edit().putString(KEY_SUBSCRIPTIONS, jsonStr).apply()
    }
}
