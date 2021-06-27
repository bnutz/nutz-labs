package com.justbnutz.labs.services

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import com.justbnutz.labs.R

abstract class QuickSettingsBaseService : TileService() {

    private fun updateTileProperties() = qsTile?.let {
        PreferenceManager.getDefaultSharedPreferences(this)?.getString(getTag(), null)?.let { pkgName ->
            packageManager?.let { pm ->
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                it.label = pm.getApplicationLabel(appInfo).toString()
                it.icon = Icon.createWithBitmap(pm.getApplicationIcon(appInfo).toBitmap(config = Bitmap.Config.ALPHA_8))
                it.updateTile()
            }
        }
    }

    // No need to keep state
    private fun keepDisabled() = qsTile?.let {
        it.state = Tile.STATE_INACTIVE
        it.updateTile()
    }

    private fun launchPackage(): String? = PreferenceManager.getDefaultSharedPreferences(this)?.getString(getTag(), null)

    abstract fun getTag(): String

    override fun onCreate() {
        super.onCreate()
        updateTileProperties()
        keepDisabled()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileProperties()
        keepDisabled()
    }

    override fun onClick() {
        super.onClick()
        try {
            launchPackage()?.let { packageName ->
                packageManager?.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
                    startActivityAndCollapse(launchIntent)
                    keepDisabled()
                }
            } ?: kotlin.run {
                val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.no_package)
                    .create()
                showDialog(dialog)
            }
        } catch (e: Exception) {
            Log.d("QS_CHECK", getString(R.string.error_message, e.message))
        }
    }
}