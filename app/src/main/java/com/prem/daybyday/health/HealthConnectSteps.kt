package com.prem.daybyday.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneId

class HealthConnectSteps(private val context: Context) {
    private val client by lazy { HealthConnectClient.getOrCreate(context) }
    val permissions = setOf(HealthPermission.getReadPermission(StepsRecord::class))

    suspend fun availability(): Int = HealthConnectClient.getSdkStatus(context)
    suspend fun hasPermission(): Boolean = client.permissionController.getGrantedPermissions().containsAll(permissions)

    suspend fun todaySteps(): Long {
        val zone = ZoneId.systemDefault()
        val start = java.time.LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val end = Instant.now()
        val result = client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), TimeRangeFilter.between(start, end)))
        return result[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    suspend fun stepsForDate(date: java.time.LocalDate): Long {
        if (date.isAfter(java.time.LocalDate.now())) return 0L
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        val result = client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), TimeRangeFilter.between(start, end)))
        return result[StepsRecord.COUNT_TOTAL] ?: 0L
    }
}
