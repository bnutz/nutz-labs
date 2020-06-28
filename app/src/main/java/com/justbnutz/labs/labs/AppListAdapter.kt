package com.justbnutz.labs.labs

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.justbnutz.labs.R
import com.justbnutz.labs.models.AppListModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.listitem_appinfo.view.*
import java.util.*

class AppListAdapter(private val compositeDisposable: CompositeDisposable) : RecyclerView.Adapter<AppListAdapter.AppListItemViewHolder>() {

    interface AppListAdapterCallback {
        fun onItemClick(packageName: String)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.listitem_appinfo, parent, false)
        return AppListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppListItemViewHolder, position: Int) {
        val itemData = dataList[position]
        holder.bind(itemData, currentSort) {
            parentCallback?.onItemClick(itemData.packageName)
        }
    }

    override fun getItemCount() = dataList.size

    class AppListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(appData: AppListModel, currentSort: AppListViewModel.SortBy, onItemClick: () -> Unit) {
            itemView.apply {
                txt_package_name?.text = appData.packageName
                img_app_icon.setImageDrawable(appData.appIcon)

                context?.let {
                    txt_app_name?.text = when (currentSort) {
                        AppListViewModel.SortBy.ALPHABETIC -> SpannableStringBuilder().bold { append(appData.appName) }
                        else -> appData.appName
                    }

                    val txtFirstInstalled = "${it.getString(R.string.first_installed)}\n${appData.firstInstalledDateString()}"
                    txt_first_installed?.text = when (currentSort) {
                        AppListViewModel.SortBy.FIRST_INSTALLED -> SpannableStringBuilder().bold { append(txtFirstInstalled) }
                        else -> txtFirstInstalled
                    }

                    val txtLastUpdated = "${it.getString(R.string.last_updated)}\n${appData.lastUpdatedDateString()}"
                    txt_last_updated?.text = when (currentSort) {
                        AppListViewModel.SortBy.LAST_UPDATED -> SpannableStringBuilder().bold { append(txtLastUpdated) }
                        else -> txtLastUpdated
                    }

                    val txtLastOpened = "${it.getString(R.string.last_opened)}\n${if (appData.lastOpened > 0) appData.lastOpenedDateString() else it.getString(R.string.empty)}"
                    txt_last_opened?.text = when (currentSort) {
                        AppListViewModel.SortBy.LAST_OPENED -> SpannableStringBuilder().bold { append(txtLastOpened) }
                        else -> txtLastOpened
                    }
                }

                setOnClickListener {
                    onItemClick.invoke()
                }
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