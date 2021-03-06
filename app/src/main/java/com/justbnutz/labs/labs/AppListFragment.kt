package com.justbnutz.labs.labs

import android.app.Activity
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.justbnutz.labs.R
import com.justbnutz.labs.databinding.FragmentApplistBinding
import com.justbnutz.labs.models.AppListModel
import java.text.SimpleDateFormat
import java.util.*

class AppListFragment : BaseFragment<FragmentApplistBinding>(), AppListAdapter.AppListAdapterCallback {

    private lateinit var viewModel: AppListViewModel
    private val listAdapter by lazy { AppListAdapter(viewModel.compositeDisposable, false) }
    private val bottomSheetBehaviour by lazy { binding?.constraintBottomsheet?.let {
        BottomSheetBehavior.from(it)
    } }

    private val appListObserver = Observer<List<AppListModel>> {
        listAdapter.updateItems(it, viewModel.currentSort.first)
        binding?.swipeApplist?.let { view ->
            view.visibility = View.VISIBLE
            view.isRefreshing = false
        }
        binding?.txtLoading?.visibility = View.GONE
    }

    private val csvListObserver = Observer<String> {
        writeCsv()
    }

    private val errorMsgObserver = Observer<String> {
        parentActivity?.showToast(getString(R.string.error_message, it))
    }

    companion object {
        val TAG: String = this::class.java.name

        private const val REQUEST_CODE_WRITE_FILE = 0x01

        fun newInstance() = AppListFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentApplistBinding {
        return FragmentApplistBinding.inflate(inflater, container, false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAppList()
    }

    override fun onItemClick(packageName: String, appName: String) {
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
                        parentActivity?.showToast(getString(R.string.error_message, "Data path was null"))
                    }
                } else {
                    viewModel.resetCsvStatus()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun initViewModel() {
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

    override fun initView() {
        binding?.imgDragHandle?.setOnClickListener {
            when (bottomSheetBehaviour?.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehaviour?.state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
                else -> {}
            }
        }

        binding?.imgSortAlphabetic?.setOnClickListener {
            viewModel.clickSort(AppListViewModel.SortBy.ALPHABETIC)
            viewModel.getAppList()
        }

        binding?.imgSortFirstInstalled?.setOnClickListener {
            viewModel.clickSort(AppListViewModel.SortBy.FIRST_INSTALLED)
            viewModel.getAppList()
        }

        binding?.imgSortLastUpdated?.setOnClickListener {
            viewModel.clickSort(AppListViewModel.SortBy.LAST_UPDATED)
            viewModel.getAppList()
        }

        binding?.imgSortLastOpened?.setOnClickListener {
            viewModel.clickSort(AppListViewModel.SortBy.LAST_OPENED)
            viewModel.getAppList()
        }

        binding?.lblSort?.setOnClickListener {
            binding?.recyclerApplist?.smoothScrollToPosition(0)
        }

        binding?.txtExportCsv?.setOnClickListener {
            initCsvExport()
        }

        binding?.recyclerApplist?.let {
            it.adapter = listAdapter
            listAdapter.setAdapterListener(this)

            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    bottomSheetBehaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            })
        }

        binding?.swipeApplist?.setOnRefreshListener {
            viewModel.getAppList()
        }

        binding?.txtOpenUsagePermissions?.setOnClickListener {
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
                        parentActivity?.showToast(getString(R.string.file_saved, filename))
                    } catch (e: Exception) {
                        parentActivity?.showToast(getString(R.string.error_message, e.message))
                    } finally {
                        buffWriter.close()
                        bottomSheetBehaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
                        viewModel.resetCsvStatus()
                    }
                }
            }
        }
    }
}