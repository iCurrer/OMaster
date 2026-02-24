package com.silas.omaster.model

import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    val url: String,
    val name: String = "",
    val isEnabled: Boolean = true,
    val presetCount: Int = 0,
    val lastUpdateTime: Long = 0
)

@Serializable
data class SubscriptionList(
    val subscriptions: List<Subscription> = emptyList()
)
