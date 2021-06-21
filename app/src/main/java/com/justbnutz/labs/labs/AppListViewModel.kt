package com.justbnutz.labs.labs

import android.app.usage.UsageStatsManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.justbnutz.labs.models.AppListModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*

class AppListViewModel : BaseViewModel() {

    enum class SortBy {
        ALPHABETIC,
        FIRST_INSTALLED,
        LAST_UPDATED,
        LAST_OPENED
    }

    enum class SortDir {
        ASC,
        DESC
    }

    var showSystemApps: Boolean = false
    var currentSort: Pair<SortBy, SortDir> = Pair(SortBy.ALPHABETIC, SortDir.ASC)
        private set

    // Pair<filename, csv text>
    var csvData: Pair<String, String>? = null
    var csvFileUri: Uri? = null

    fun resetCsvStatus() {
        csvData = null
        csvFileUri = null
    }

    lateinit var packageManager: PackageManager
    lateinit var usageManager: UsageStatsManager

    private val lastYear by lazy {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        calendar.timeInMillis
    }

    private val _appList by lazy { MutableLiveData<List<AppListModel>>() }
    val appList: LiveData<List<AppListModel>>
        get() = _appList

    private val _csvList by lazy { MutableLiveData<String>() }
    val csvList: LiveData<String>
        get() = _csvList

    private val _errorMsg by lazy { MutableLiveData<String>() }
    val errorMsg: LiveData<String>
        get() = _errorMsg

    fun getAppList() {
        Observable.just(packageManager)
            .subscribeOn(Schedulers.io())
            .map { pm ->
                val appList = mutableListOf<AppListModel>()

                // References:
                // - https://github.com/googlesamples/android-AppUsageStatistics
                // - https://stackoverflow.com/a/50559880
                val usageStats = usageManager.queryAndAggregateUsageStats(lastYear, System.currentTimeMillis())

                val installedAppList =
                    if (showSystemApps) pm.getInstalledApplications(PackageManager.GET_META_DATA).filterNotNull()
                    else pm.getInstalledApplications(PackageManager.GET_META_DATA).filterNotNull().filter { (it.flags.and(ApplicationInfo.FLAG_SYSTEM)) != ApplicationInfo.FLAG_SYSTEM }

                installedAppList.forEach { appInfo ->
                    appInfo.packageName?.let { packageName ->
                        // Needed for fetching install and update timestamps
                        val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)

                        appList.add(
                            AppListModel(
                                pm.getApplicationLabel(appInfo).toString(),
                                pm.getApplicationIcon(appInfo),
                                packageName,
                                (appInfo.flags.and(ApplicationInfo.FLAG_SYSTEM)) == ApplicationInfo.FLAG_SYSTEM,
                                packageInfo.firstInstallTime,
                                packageInfo.lastUpdateTime,
                                usageStats[packageName]?.lastTimeUsed ?: 0,
                                appInfo.dataDir,
                                appInfo.nativeLibraryDir,
                                appInfo.publicSourceDir,
                                appInfo.sourceDir
                            )
                        )
                    }
                }

                appList
            }
            .observeOn(Schedulers.computation())
            .map { appList ->
                when (currentSort.second) {
                    SortDir.ASC -> {
                        when (currentSort.first) {
                            SortBy.ALPHABETIC -> appList.sortedBy { it.appName }
                            SortBy.FIRST_INSTALLED -> appList.sortedBy { it.firstInstalled }
                            SortBy.LAST_UPDATED -> appList.sortedBy { it.lastUpdated }
                            SortBy.LAST_OPENED -> shiftEmptyDates(appList.sortedBy { it.lastUpdated }.sortedBy { it.lastOpened })
                        }
                    }
                    SortDir.DESC -> {
                        when (currentSort.first) {
                            SortBy.ALPHABETIC -> appList.sortedByDescending { it.appName }
                            SortBy.FIRST_INSTALLED -> appList.sortedByDescending { it.firstInstalled }
                            SortBy.LAST_UPDATED -> appList.sortedByDescending { it.lastUpdated }
                            SortBy.LAST_OPENED -> shiftEmptyDates(appList.sortedByDescending { it.lastUpdated }.sortedByDescending { it.lastOpened })
                        }
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    it?.let { sortedList ->
                        _appList.value = sortedList
                    } ?: throw Error("App List failed")
                },
                onError = { error ->
                    _errorMsg.value = error.message
                }
            )
            .addTo(compositeDisposable)
    }

    private fun shiftEmptyDates(appList: List<AppListModel>): List<AppListModel> {
        return appList
            .filter { it.lastOpened > 0 }
            .plus(appList.filter { it.lastOpened <= 0 })
    }

    fun clickSort(newSort: SortBy) {
        currentSort =
                // If already on tis sort, flip the direction
            if (currentSort.first == newSort) currentSort.copy(
                second = when (currentSort.second) {
                    SortDir.ASC -> SortDir.DESC
                    SortDir.DESC -> SortDir.ASC
                }
            )
            // If a different sort, then switch to it (keep the direction)
            else currentSort.copy(first = newSort)
    }

    fun createCsvList(filename: String) {
        Observable.just(filename)
            .subscribeOn(Schedulers.computation())
            .map {
                val sBuilder = StringBuilder()
                sBuilder.append("\"Package Name\",")
                sBuilder.append("\"App Name\",")
                sBuilder.append("\"First Installed\",")
                sBuilder.append("\"Last Updated\",")
                sBuilder.append("\"Last Opened\",")
                sBuilder.append("\"System App\",")
                sBuilder.append("\"Data Directory\",")
                sBuilder.append("\"Native Library Directory\",")
                sBuilder.append("\"Public Source Directory\",")
                sBuilder.append("\"Source Directory\"")
                sBuilder.appendln()

                _appList.value?.forEach {
                    sBuilder.append("\"${it.packageName}\",")
                    sBuilder.append("\"${it.appName.replace("\"", "'")}\",")
                    sBuilder.append("\"${if (it.firstInstalled > 0) it.firstInstalledDateString() else ""}\",")
                    sBuilder.append("\"${if (it.lastUpdated > 0) it.lastUpdatedDateString() else ""}\",")
                    sBuilder.append("\"${if (it.lastOpened > 0) it.lastOpenedDateString() else ""}\",")
                    sBuilder.append("\"${it.isSystemApp}\",")
                    sBuilder.append("\"${it.dataDir}\",")
                    sBuilder.append("\"${it.nativeLibraryDir}\",")
                    sBuilder.append("\"${it.publicSourceDir}\",")
                    sBuilder.append("\"${it.sourceDir}\"")
                    sBuilder.appendln()
                }

                Pair(filename, sBuilder.toString())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    csvData = it
                    _csvList.value = filename
                },
                onError = { error ->
                    resetCsvStatus()
                    _errorMsg.value = error.message
                }
            )
            .addTo(compositeDisposable)
    }
}