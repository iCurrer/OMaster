package com.example.omaster

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.umeng.commonsdk.UMConfigure

class OMasterApplication : Application() {
    companion object {
        private const val PREFS_NAME = "omaster_prefs"
        private const val KEY_USER_AGREED = "user_agreed_to_policy"
        private const val KEY_UMENG_INITIALIZED = "umeng_initialized"

        private lateinit var instance: OMasterApplication
        private lateinit var prefs: SharedPreferences

        fun getInstance(): OMasterApplication = instance
        fun getPrefs(): SharedPreferences = prefs
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        preInitUMeng()

        if (hasUserAgreed() && !isUMengInitialized()) {
            initUMeng()
        }
    }

    private fun preInitUMeng() {
        UMConfigure.setLogEnabled(true)
        UMConfigure.preInit(this, "698938eb9a7f3764885bbdaa", "default")
    }

    fun initUMeng() {
        UMConfigure.init(this, "698938eb9a7f3764885bbdaa", "default", UMConfigure.DEVICE_TYPE_PHONE, null)
        setUMengInitialized(true)
    }

    fun hasUserAgreed(): Boolean {
        return prefs.getBoolean(KEY_USER_AGREED, false)
    }

    fun setUserAgreed(agreed: Boolean) {
        prefs.edit().putBoolean(KEY_USER_AGREED, agreed).apply()
    }

    private fun isUMengInitialized(): Boolean {
        return prefs.getBoolean(KEY_UMENG_INITIALIZED, false)
    }

    private fun setUMengInitialized(initialized: Boolean) {
        prefs.edit().putBoolean(KEY_UMENG_INITIALIZED, initialized).apply()
    }


}
