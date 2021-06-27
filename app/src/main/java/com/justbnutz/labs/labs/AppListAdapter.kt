package com.justbnutz.labs.labs

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.justbnutz.labs.R
import com.justbnutz.labs.databinding.ListitemAppSimpleBinding
import com.justbnutz.labs.databinding.ListitemAppinfoBinding
import com.justbnutz.labs.models.AppListModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.util.*

class AppListAdapter(private val compositeDisposable: CompositeDisposable, private val simpleRows: Boolean) : RecyclerView.Adapter<AppListAdapter.AppListBaseViewHolder>() {

    interface AppListAdapterCallback {
        fun onItemClick(packageName: String, appName: String)
    }

    var parentCallback: AppListAdapterCallback? = null

    fun setAdapterListener(adapterCallback: AppListAdapterCallback) {
        parentCallback = adapterCallback
    }

    private lateinit var currentSort: AppListViewModel.SortBy
    private var dataList = listOf<AppListModel>()
    private val pendingUpdates = ArrayDeque<List<AppListModel>>()

    fun updateItems(newList: List<AppListModel>, newSort: AppListViewModel.SortBy) {
        currentSort = newSort

        pendingUpdates.add(newList)
        pendingUpdates.singleOrNull()?.let {
            updateItemsInternal(it)
        }
    }

    private fun updateItemsInternal(nextList: List<AppListModel>) {
        Observable.just(Pair(dataList, nextList))
            .subscribeOn(Schedulers.computation())
            .map { (oldList, newList) ->
                val callback =
                    DiffUtilCallback(
                        oldList,
                        newList
                    )
                DiffUtil.calculateDiff(callback, true)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                pendingUpdates.peekLast()?.let {
                    pendingUpdates.clear()
                    pendingUpdates.add(it)
                    updateItemsInternal(it)
                }
            }
            .subscribe { diffResult ->
                pendingUpdates.remove(nextList)
                dataList = nextList
                diffResult?.dispatchUpdatesTo(this)
            }
            .addTo(compositeDisposable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppListBaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (simpleRows) {
            val itemViewBinding = ListitemAppSimpleBinding.inflate(inflater, parent, false)
            return AppListSimpleViewHolder(itemViewBinding)
        } else {
            val itemViewBinding = ListitemAppinfoBinding.inflate(inflater, parent, false)
            return AppListItemViewHolder(itemViewBinding)
        }
    }

    override fun onBindViewHolder(holder: AppListBaseViewHolder, position: Int) {
        val itemData = dataList[position]
        holder.bind(itemData, currentSort) {
            parentCallback?.onItemClick(itemData.packageName, itemData.appName)
        }
    }

    override fun getItemCount() = dataList.size

    class AppListSimpleViewHolder(private val itemViewBinding: ListitemAppSimpleBinding) : AppListBaseViewHolder(itemViewBinding) {
        override fun bind(appData: AppListModel, currentSort: AppListViewModel.SortBy, onItemClick: () -> Unit) {
            itemViewBinding.apply {
                txtAppName.text = appData.appName
                txtPackageName.text = appData.packageName
                imgAppIcon.setImageDrawable(appData.appIcon)

            }
            super.bind(appData, currentSort, onItemClick)
        }
    }

    class AppListItemViewHolder(private val itemViewBinding: ListitemAppinfoBinding) : AppListBaseViewHolder(itemViewBinding) {
        override fun bind(appData: AppListModel, currentSort: AppListViewModel.SortBy, onItemClick: () -> Unit) {
            itemViewBinding.apply {
                txtPackageName.text = appData.packageName
                imgAppIcon.setImageDrawable(appData.appIcon)

                itemViewBinding.root.context?.let {
                    txtAppName.text = when (currentSort) {
                        AppListViewModel.SortBy.ALPHABETIC -> SpannableStringBuilder().bold { append(appData.appName) }
                        else -> appData.appName
                    }

                    val lblFirstInstalled = "${it.getString(R.string.first_installed)}\n${appData.firstInstalledDateString()}"
                    txtFirstInstalled.text = when (currentSort) {
                        AppListViewModel.SortBy.FIRST_INSTALLED -> SpannableStringBuilder().bold { append(lblFirstInstalled) }
                        else -> lblFirstInstalled
                    }

                    val lblLastUpdated = "${it.getString(R.string.last_updated)}\n${appData.lastUpdatedDateString()}"
                    txtLastUpdated.text = when (currentSort) {
                        AppListViewModel.SortBy.LAST_UPDATED -> SpannableStringBuilder().bold { append(lblLastUpdated) }
                        else -> lblLastUpdated
                    }

                    val lblLastOpened = "${it.getString(R.string.last_opened)}\n${if (appData.lastOpened > 0) appData.lastOpenedDateString() else it.getString(R.string.empty)}"
                    txtLastOpened.text = when (currentSort) {
                        AppListViewModel.SortBy.LAST_OPENED -> SpannableStringBuilder().bold { append(lblLastOpened) }
                        else -> lblLastOpened
                    }
                }
            }
            super.bind(appData, currentSort, onItemClick)
        }
    }

    abstract class AppListBaseViewHolder(private val itemViewBinding: ViewBinding) : RecyclerView.ViewHolder(itemViewBinding.root) {
        open fun bind(appData: AppListModel, currentSort: AppListViewModel.SortBy, onItemClick: () -> Unit) {
            itemViewBinding.root.setOnClickListener {
                onItemClick.invoke()
            }
        }
    }

    private class DiffUtilCallback(
        val oldList: List<AppListModel>,
        val newList: List<AppListModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.appName == newItem.appName
                    && oldItem.appIcon == newItem.appIcon
                    && oldItem.isSystemApp == newItem.isSystemApp
                    && oldItem.firstInstalled == newItem.firstInstalled
                    && oldItem.lastUpdated == newItem.lastUpdated
                    && oldItem.lastOpened == newItem.lastOpened
                    && oldItem.dataDir == newItem.dataDir
                    && oldItem.nativeLibraryDir == newItem.nativeLibraryDir
                    && oldItem.publicSourceDir == newItem.publicSourceDir
                    && oldItem.sourceDir == newItem.sourceDir
        }
    }
}