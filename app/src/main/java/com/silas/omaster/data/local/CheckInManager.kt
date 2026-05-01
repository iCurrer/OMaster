package com.silas.omaster.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 打卡记录数据类
 */
data class CheckInRecord(
    val date: String,
    val hasDrawnCard: Boolean = false,
    val cardId: String? = null,
    val cardTheme: String? = null,
    val hasCompletedChallenge: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 打卡管理器
 * 负责记录用户每日打卡行为，支持连续天数统计、月度热力图等
 */
class CheckInManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "check_in_prefs"
        private const val KEY_CHECK_IN_RECORDS = "check_in_records"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_LAST_CHECK_IN_DATE = "last_check_in_date"
        private const val DATE_FORMAT = "yyyy-MM-dd"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT)
    }

    /**
     * 获取所有打卡记录
     */
    fun getAllRecords(): Map<String, CheckInRecord> {
        val json = prefs.getString(KEY_CHECK_IN_RECORDS, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, CheckInRecord>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    /**
     * 保存打卡记录
     */
    private fun saveRecords(records: Map<String, CheckInRecord>) {
        val json = gson.toJson(records)
        prefs.edit().putString(KEY_CHECK_IN_RECORDS, json).apply()
    }

    /**
     * 打卡（抽色卡时自动调用）
     */
    fun checkIn(cardId: String? = null, cardTheme: String? = null) {
        val today = LocalDate.now().format(DATE_FORMATTER)
        val records = getAllRecords().toMutableMap()

        val existingRecord = records[today]
        val newRecord = if (existingRecord != null) {
            existingRecord.copy(
                hasDrawnCard = true,
                cardId = cardId ?: existingRecord.cardId,
                cardTheme = cardTheme ?: existingRecord.cardTheme,
                timestamp = System.currentTimeMillis()
            )
        } else {
            CheckInRecord(
                date = today,
                hasDrawnCard = true,
                cardId = cardId,
                cardTheme = cardTheme
            )
        }

        records[today] = newRecord
        saveRecords(records)

        // 更新连续打卡天数
        updateStreak()
    }

    /**
     * 标记完成挑战
     */
    fun markChallengeCompleted() {
        val today = LocalDate.now().format(DATE_FORMATTER)
        val records = getAllRecords().toMutableMap()

        val existingRecord = records[today]
        val newRecord = if (existingRecord != null) {
            existingRecord.copy(hasCompletedChallenge = true, timestamp = System.currentTimeMillis())
        } else {
            CheckInRecord(date = today, hasCompletedChallenge = true)
        }

        records[today] = newRecord
        saveRecords(records)
        updateStreak()
    }

    /**
     * 获取今日打卡记录
     */
    fun getTodayRecord(): CheckInRecord? {
        val today = LocalDate.now().format(DATE_FORMATTER)
        return getAllRecords()[today]
    }

    /**
     * 获取指定日期的打卡记录
     */
    fun getRecordForDate(date: LocalDate): CheckInRecord? {
        val dateStr = date.format(DATE_FORMATTER)
        return getAllRecords()[dateStr]
    }

    /**
     * 获取指定月份的打卡记录
     */
    fun getRecordsForMonth(year: Int, month: Int): Map<String, CheckInRecord> {
        val allRecords = getAllRecords()
        val prefix = "$year-${month.toString().padStart(2, '0')}"
        return allRecords.filterKeys { it.startsWith(prefix) }
    }

    /**
     * 获取当前连续打卡天数
     */
    fun getCurrentStreak(): Int {
        return prefs.getInt(KEY_CURRENT_STREAK, 0)
    }

    /**
     * 获取历史最长连续打卡天数
     */
    fun getLongestStreak(): Int {
        return prefs.getInt(KEY_LONGEST_STREAK, 0)
    }

    /**
     * 更新连续打卡天数
     */
    private fun updateStreak() {
        val today = LocalDate.now()
        val lastDateStr = prefs.getString(KEY_LAST_CHECK_IN_DATE, null)
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)

        val newStreak = if (lastDateStr == null) {
            1
        } else {
            val lastDate = LocalDate.parse(lastDateStr, DATE_FORMATTER)
            val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastDate, today)

            when {
                daysBetween == 1L -> currentStreak + 1
                daysBetween == 0L -> currentStreak
                else -> 1
            }
        }

        val newLongest = maxOf(newStreak, longestStreak)

        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .putInt(KEY_LONGEST_STREAK, newLongest)
            .putString(KEY_LAST_CHECK_IN_DATE, today.format(DATE_FORMATTER))
            .apply()
    }

    /**
     * 获取本月打卡天数
     */
    fun getMonthlyCheckInCount(year: Int, month: Int): Int {
        return getRecordsForMonth(year, month).size
    }

    /**
     * 获取本月完成挑战次数
     */
    fun getMonthlyChallengeCount(year: Int, month: Int): Int {
        return getRecordsForMonth(year, month).values.count { it.hasCompletedChallenge }
    }

    /**
     * 清空所有打卡记录（用于测试）
     */
    fun clearAllRecords() {
        prefs.edit()
            .remove(KEY_CHECK_IN_RECORDS)
            .remove(KEY_CURRENT_STREAK)
            .remove(KEY_LONGEST_STREAK)
            .remove(KEY_LAST_CHECK_IN_DATE)
            .apply()
    }
}
