package com.justbnutz.labs.labs

import android.app.Activity
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.justbnutz.labs.R
import com.justbnutz.labs.models.AppListModel
import kotlinx.android.synthetic.main.fragment_applist.*
import java.text.SimpleDateFormat
import java.util.*

class AppListFragment : BaseFragment(), AppListAdapter.AppListAdapterCallback {

    private lateinit var viewModel: AppListViewModel
    private val listAdapter by lazy { AppListAdapter(viewModel.compositeDisposable) }
    private val bottomSheetBehaviour by lazy { BottomSheetBehavior.from(constraint_bottomsheet) }

    private val appListObserver = Observer<List<AppListModel>> {
        listAdapter.updateItems(it, viewModel.currentSort.first)
        swipe_applist?.let {
            it.visibility = View.VISIBLE
            it.isRefreshing = false
        }
        txt_loading?.visibility = View.GONE
    }

    private val csvListObserver = Observer<String> {
        writeCsv()
    }

    private val errorMsgObserver = Observer<String> {
        parentActivity?.showToast(String.format(getString(R.string.error_message), it))
    }

    companion object {
        val TAG = "${this::class.qualifiedName}"

        private const val REQUEST_CODE_WRITE_FILE = 0x01

        fun newInstance() = AppListFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_applist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAppList()
    }

    override fun onItemClick(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
            it.addCategory(Intent.CATEGORY_DEFAULT)
            it.data = Uri.parse("package:$packageName")
        }
        parentActivity?.runGenericIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_WRITE_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        viewModel.csvFileUri = it
                        writeCsv()
                    } ?: run {
                        viewModel.resetCsvStatus()
                        parentActivity?.showToast(String.format(getString(R.string.error_message), "Data path was null"))
                    }
                } else {
                    viewModel.resetCsvStatus()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel = ViewModelProvider(this).get(AppListViewModel::class.java)
        viewModel.appList.observe(this, appListObserver)
        viewModel.csvList.observe(this, csvListObserver)
        viewModel.errorMsg.observe(this, errorMsgObserver)

        context?.let { _context ->
            _context.packageManager?.let {
                viewModel.packageManager = it
            }
            (_context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager)?.let {
                viewModel.usageManager = it
            }
        }
    }

    private fun initView() {
        img_drag_handle?.setOnClickListener {
            when (bottomSheetBehaviour.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                else -> {}
            }
        }

        img_sort_alphabetic?.setOnClickListener {
            viewModel.clickSort(AppListViewModel.SortBy.ALPHABETIC)
            viewModel.getAppList()
        }

        img_sort_first_installed?.setOnClickListener {
            viewModel.clickSort(AppListViewModel.SortBy.FIRST_INSTALLED)
            viewModel.getAppList()
        }

        img_sort_last_updated?.setOnClickListener {
            viewModel.clickSort(AppListViewModel.SortBy.LAST_UPDATED)
            viewModel.getAppList()
        }

        img_sort_last_opened?.setOnClickListener {
            viewModel.clickSort(AppListViewModel.SortBy.LAST_OPENED)
            viewModel.getAppList()
        }

        lbl_sort?.setOnClickListener {
            recycler_applist?.smoothScrollToPosition(0)
        }

        switch_show_system_apps?.let {
            it.isChecked = viewModel.showSystemApps
            it.setOnCheckedChangeListener { _, isChecked ->
                viewModel.showSystemApps = isChecked
                viewModel.getAppList()
            }
        }

        txt_export_csv?.setOnClickListener {
            initCsvExport()
        }

        recycler_applist?.let {
            it.adapter = listAdapter
            listAdapter.setAdapterListener(this)

            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            })
        }

        swipe_applist?.setOnRefreshListener {
            viewModel.getAppList()
        }

        txt_open_usage_permissions?.setOnClickListener {
            parentActivity?.runGenericIntent(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            )
        }
    }

    // https://developer.android.com/training/data-storage/shared/documents-files#create-file
    private fun initCsvExport() {
        viewModel.resetCsvStatus()

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.ENGLISH).format(Date())
        val filename = "app_list-$timestamp.csv"
        viewModel.createCsvList(filename)

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, filename)
        }

        startActivityForResult(intent, REQUEST_CODE_WRITE_FILE)
    }

    private fun writeCsv() {
        viewModel.csvFileUri?.let { fileUri ->
            viewModel.csvData?.let { (filename, csvDataString) ->
                context?.contentResolver?.openOutputStream(fileUri)?.bufferedWriter()?.use { buffWriter ->
                    try {
                        buffWriter.write(csvDataString)
                        buffWriter.flush()
                        parentActivity?.showToast(String.format(getString(R.string.file_saved), filename))
                    } catch (e: Exception) {
                        parentActivity?.showToast(String.format(getString(R.string.error_message), e.message))
                    } finally {
                        buffWriter.close()
                        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                        viewModel.resetCsvStatus()
                    }
                }
            }
        }
    }
}