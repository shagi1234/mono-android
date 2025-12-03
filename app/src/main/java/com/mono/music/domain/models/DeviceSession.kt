package com.mono.music.domain.models

import com.google.gson.annotations.SerializedName

data class DeviceSession(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("device_name")
    val deviceName: String,
    @SerializedName("device_type")
    val deviceType: String,
    val platform: String,
    @SerializedName("login_time")
    val loginTime: String,
    @SerializedName("last_activity")
    val lastActivity: String,
    @SerializedName("ip_address")
    val ipAddress: String,
    val city: String,
    val country: String,
    @SerializedName("app_version")
    val appVersion: String,
    @SerializedName("os_version")
    val osVersion: String,
    @SerializedName("is_current_session")
    val isCurrentSession: Boolean
)

data class DeviceSummary(
    val web: Int = 0,
    val mobile: Int = 0,
    val tablet: Int = 0,
    val desktop: Int = 0
)

data class DeviceLimits(
    val mobile: Int,
    val web: Int,
    val tablet: Int,
    val desktop: Int
)

data class ActiveSessionsResponse(
    @SerializedName("active_sessions")
    val activeSessions: List<DeviceSession>,
    @SerializedName("total_sessions")
    val totalSessions: Int,
    @SerializedName("device_summary")
    val deviceSummary: DeviceSummary,
    val limits: DeviceLimits
)
