package com.silas.omaster.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.silas.omaster.data.config.ConfigCenter
import com.silas.omaster.data.local.AppLanguage
import java.util.Locale

/**
 * 语言配置工具类
 */
object LanguageUtil {

    /**
     * 应用语言设置
     * 在 Activity 创建前调用
     */
    fun applyLanguage(context: Context): Context {
        val config = ConfigCenter.getInstance(context)
        val language = config.appLanguage
        return updateResources(context, language)
    }

    /**
     * 获取当前应该使用的 Locale
     */
    fun getLocale(context: Context): Locale {
        val config = ConfigCenter.getInstance(context)
        return when (config.appLanguage) {
            AppLanguage.SYSTEM -> getSystemLocale()
            AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }
    }

    /**
     * 更新资源配置
     */
    private fun updateResources(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.SYSTEM -> getSystemLocale()
            AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    /**
     * 获取系统默认语言
     */
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.getDefault(Locale.Category.DISPLAY)
        } else {
            @Suppress("DEPRECATION")
            Locale.getDefault()
        }
    }
}
