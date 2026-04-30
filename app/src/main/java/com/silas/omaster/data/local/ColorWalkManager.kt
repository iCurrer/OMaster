package com.silas.omaster.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silas.omaster.data.ColorCardLibrary
import com.silas.omaster.model.ColorCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Color Walk 管理器
 * 管理每日色卡抽取和浏览历史
 * 
 * 【存储方案】
 * 使用 SharedPreferences 存储：
 * - 今日色卡 ID
 * - 抽取日期
 * - 浏览历史（最近 7 天）
 */
class ColorWalkManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    private val _todayCardFlow: MutableStateFlow<ColorCard?>
    val todayCardFlow: StateFlow<ColorCard?>

    private val _historyFlow: MutableStateFlow<List<ColorCardHistory>>
    val historyFlow: StateFlow<List<ColorCardHistory>>

    private val _drawCountFlow: MutableStateFlow<Int>
    val drawCountFlow: StateFlow<Int>

    init {
        _todayCardFlow = MutableStateFlow(loadTodayCard())
        todayCardFlow = _todayCardFlow.asStateFlow()

        _historyFlow = MutableStateFlow(loadHistory())
        historyFlow = _historyFlow.asStateFlow()

        _drawCountFlow = MutableStateFlow(loadDrawCount())
        drawCountFlow = _drawCountFlow.asStateFlow()
    }

    /**
     * 获取今日色卡
     * 如果今天还没有抽取，返回 null
     */
    fun getTodayCard(): ColorCard? {
        val savedDate = prefs.getString(KEY_TODAY_DATE, null)
        val today = LocalDate.now().format(DATE_FORMATTER)

        if (savedDate == today) {
            val cardId = prefs.getString(KEY_TODAY_CARD_ID, null)
            return cardId?.let { ColorCardLibrary.getCardById(it) }
        }

        return null
    }

    /**
     * 抽取今日色卡
     * 每日最多 3 次抽取机会（0:00 刷新）
     */
    fun drawTodayCard(): ColorCard? {
        val today = LocalDate.now().format(DATE_FORMATTER)
        val savedDate = prefs.getString(KEY_TODAY_DATE, null)
        val isFirstDraw = savedDate != today

        // 检查是否还有抽取机会
        if (!isFirstDraw && getDrawCount() >= MAX_DRAWS_PER_DAY) {
            return null
        }

        val card = ColorCardLibrary.getRandomCard()

        prefs.edit()
            .putString(KEY_TODAY_DATE, today)
            .putString(KEY_TODAY_CARD_ID, card.id)
            .apply()

        _todayCardFlow.value = card

        // 更新抽取次数
        val newCount = if (isFirstDraw) 1 else getDrawCount() + 1
        prefs.edit().putInt(KEY_DRAW_COUNT, newCount).apply()
        _drawCountFlow.value = newCount

        // 每次抽取都添加到历史记录
        addToHistory(card)

        return card
    }

    /**
     * 清除今日色卡
     * 用户可以重置，但不恢复抽取次数
     */
    fun clearTodayCard() {
        prefs.edit()
            .remove(KEY_TODAY_DATE)
            .remove(KEY_TODAY_CARD_ID)
            .apply()

        _todayCardFlow.value = null
    }

    /**
     * 清空历史记录
     */
    fun clearHistory() {
        prefs.edit()
            .remove(KEY_HISTORY)
            .apply()

        _historyFlow.value = emptyList()
    }

    /**
     * 检查今天是否已抽取色卡
     */
    fun hasDrawnToday(): Boolean {
        val savedDate = prefs.getString(KEY_TODAY_DATE, null)
        val today = LocalDate.now().format(DATE_FORMATTER)
        return savedDate == today && prefs.contains(KEY_TODAY_CARD_ID)
    }

    /**
     * 获取今日已抽取次数
     */
    fun getDrawCount(): Int {
        val savedDate = prefs.getString(KEY_TODAY_DATE, null)
        val today = LocalDate.now().format(DATE_FORMATTER)

        return if (savedDate == today) {
            prefs.getInt(KEY_DRAW_COUNT, 0)
        } else {
            0
        }
    }

    /**
     * 检查是否还有抽取机会
     */
    fun canDrawMore(): Boolean {
        return getDrawCount() < MAX_DRAWS_PER_DAY
    }

    /**
     * 获取剩余抽取次数
     */
    fun getRemainingDraws(): Int {
        return maxOf(0, MAX_DRAWS_PER_DAY - getDrawCount())
    }

    /**
     * 添加到浏览历史
     * 保留最近 30 条记录
     */
    private fun addToHistory(card: ColorCard) {
        val history = loadHistory().toMutableList()

        history.add(0, ColorCardHistory(card.id, LocalDate.now().format(DATE_FORMATTER)))

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }

        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
        _historyFlow.value = history
    }

    fun getHistory(): List<ColorCardHistory> = loadHistory()

    private fun loadHistory(): List<ColorCardHistory> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<ColorCardHistory>>() {}.type
        return try {
            gson.fromJson(raw, type) ?: emptyList()
        } catch (e: Exception) {
            try {
                val legacy = raw.split(",").mapNotNull { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) ColorCardHistory(parts[0], parts[1]) else null
                }
                if (legacy.isNotEmpty()) {
                    val newJson = gson.toJson(legacy)
                    prefs.edit().putString(KEY_HISTORY, newJson).apply()
                }
                legacy
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private fun loadTodayCard(): ColorCard? {
        val savedDate = prefs.getString(KEY_TODAY_DATE, null)
        val today = LocalDate.now().format(DATE_FORMATTER)

        if (savedDate == today) {
            val cardId = prefs.getString(KEY_TODAY_CARD_ID, null)
            return cardId?.let { ColorCardLibrary.getCardById(it) }
        }

        return null
    }

    private fun loadDrawCount(): Int {
        return getDrawCount()
    }

    companion object {
        private const val PREFS_NAME = "color_walk_prefs"
        private const val KEY_TODAY_DATE = "today_date"
        private const val KEY_TODAY_CARD_ID = "today_card_id"
        private const val KEY_HISTORY = "history"
        private const val KEY_DRAW_COUNT = "draw_count"
        private const val MAX_HISTORY_SIZE = 30
        private const val MAX_DRAWS_PER_DAY = 3
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}

/**
 * 色卡历史记录
 */
data class ColorCardHistory(
    val cardId: String,
    val date: String
) {
    fun getColorCard(): ColorCard? {
        return ColorCardLibrary.getCardById(cardId)
    }
}
