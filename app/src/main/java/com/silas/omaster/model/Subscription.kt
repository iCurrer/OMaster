package com.silas.omaster.model

import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    val url: String,
    val name: String = "",
    val author: String = "",
    val build: Int = 1,
    val isEnabled: Boolean = true,
    val presetCount: Int = 0,
    val lastUpdateTime: Long = 0
)

@Serializable
data class SubscriptionList(
    val subscriptions: List<Subscription> = emptyList()
)
