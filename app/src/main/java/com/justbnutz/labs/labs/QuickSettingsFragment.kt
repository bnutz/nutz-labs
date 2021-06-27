package com.justbnutz.labs.labs

import android.app.AlertDialog
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.justbnutz.labs.R
import com.justbnutz.labs.databinding.FragmentQuicksettingsBinding
import com.justbnutz.labs.models.AppListModel
import com.justbnutz.labs.services.QuickSettingsService1
import com.justbnutz.labs.services.QuickSettingsService2

class QuickSettingsFragment : BaseFragment<FragmentQuicksettingsBinding>(), AppListAdapter.AppListAdapterCallback {

    private lateinit var viewModel: QuickSettingsViewModel
    private val listAdapter by lazy { AppListAdapter(viewModel.compositeDisposable, true) }

    private val appListObserver = Observer<List<AppListModel>> {
        listAdapter.updateItems(it, viewModel.currentSort.first)
        binding?.swipeApplist?.let { view ->
            view.visibility = View.VISIBLE
            view.isRefreshing = false
        }
        binding?.txtLoading?.visibility = View.GONE
    }

    private val errorMsgObserver = Observer<String> {
        parentActivity?.showToast(getString(R.string.error_message, it))
    }

    companion object {
        val TAG: String = this::class.java.name

        fun newInstance() = QuickSettingsFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentQuicksettingsBinding {
        return FragmentQuicksettingsBinding.inflate(inflater, container, false)
    }

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel = ViewModelProvider(this).get(QuickSettingsViewModel::class.java).also {
            it.appList.observe(this, appListObserver)
            it.errorMsg.observe(this, errorMsgObserver)
        }

        context?.let { _context ->
            _context.packageManager?.let {
                viewModel.packageManager = it
            }

            (_context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager)?.let {
                viewModel.usageManager = it
            }
        }
    }

    override fun initView() {
        binding?.apply {
            context?.let { _context ->
                switchToggleQuicksettingTile1.let {
                    val componentName = ComponentName(_context, QuickSettingsService1::class.java)
                    toggleQsTile(it, componentName)
                }

                switchToggleQuicksettingTile2.let {
                    val componentName = ComponentName(_context, QuickSettingsService2::class.java)
                    toggleQsTile(it, componentName)
                }

                PreferenceManager.getDefaultSharedPreferences(_context)?.let {
                    val defaultPrompt = getString(R.string.quicksetting_target_prompt)
                    txtQuicksetting1Package.text = it.getString(QuickSettingsService1.TAG, defaultPrompt)
                    txtQuicksetting2Package.text = it.getString(QuickSettingsService2.TAG, defaultPrompt)
                }
            }

            recyclerApplist.let {
                it.adapter = listAdapter
                listAdapter.setAdapterListener(this@QuickSettingsFragment)
            }

            swipeApplist.setOnRefreshListener {
                viewModel.getAppList()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAppList()
    }

    override fun onItemClick(packageName: String, appName: String) {
        context?.apply {
            AlertDialog.Builder(this)
                .setTitle(R.string.quicksettings_select_title)
                .setSingleChoiceItems(R.array.quicksettings_labels, -1) { dialog, which ->
                    when (which) {
                        0 -> setTileProperties(QuickSettingsService1.TAG, packageName, appName, binding?.txtQuicksetting1Package)
                        1 -> setTileProperties(QuickSettingsService2.TAG, packageName, appName, binding?.txtQuicksetting2Package)
                        else -> {}
                    }
                    dialog?.dismiss()
                }
                .show()
        }
    }

    private fun toggleQsTile(switch: SwitchMaterial, componentName: ComponentName) {
        val tileState = viewModel.packageManager.getComponentEnabledSetting(componentName)

        switch.isChecked = (tileState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)

        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            // TODO: Remove tile if added, or prompt user to refresh by opening edit
            viewModel.packageManager.setComponentEnabledSetting(
                componentName,
                if (isChecked) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    private fun setTileProperties(serviceTag: String, packageName: String, appName: String, txtView: TextView?) {
        PreferenceManager.getDefaultSharedPreferences(context)?.edit()
            ?.putString(serviceTag, packageName)
            ?.apply()

        txtView?.text = packageName
    }
}