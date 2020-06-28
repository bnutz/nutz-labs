package com.justbnutz.labs.models

import android.graphics.drawable.Drawable
import java.text.SimpleDateFormat
import java.util.*

data class AppListModel(
    val appName: String,
    val appIcon: Drawable?,
    val packageName: String,

    val isSystemApp: Boolean,

    val firstInstalled: Long,
    val lastUpdated: Long,
    val lastOpened: Long,

    val dataDir: String?,
    val nativeLibraryDir: String?,
    val publicSourceDir: String?,
    val sourceDir: String?
) {
    fun firstInstalledDateString() = epochToDate(firstInstalled)
    fun lastUpdatedDateString() = epochToDate(lastUpdated)
    fun lastOpenedDateString() = epochToDate(lastOpened)

    private fun epochToDate(epoch: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        val date = Date(epoch)
        return simpleDateFormat.format(date)
    }
}