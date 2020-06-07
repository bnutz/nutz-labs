package com.justbnutz.labs.labs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.justbnutz.labs.models.WikiModel
import com.justbnutz.labs.services.WikipediaApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class WikiViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val apiService by lazy { WikipediaApiService.create() }

    private val _response by lazy { MutableLiveData<String>() }
    val response: LiveData<String>
        get() = _response

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun beginSearch(srsearch: String) {
        _response.value = "- Loading..."
        apiService.hitCountCheck("query", "json", "search", srsearch)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { response -> showResult(response) },
                { error -> showError(error.message) }
            )
            ?.addTo(compositeDisposable)
    }

    fun showResult(result: WikiModel?) {
        with (_response) {
            value = "--- RESPONSE START ---"
            value = "batchcomplete: ${result?.batchcomplete}"
            value = "continue.continue: ${result?.cont?.cont}"
            value = "continue.sroffset: ${result?.cont?.sroffset}"
            value = "query.searchinfo.totalhits: ${result?.query?.searchinfo?.totalhits}"
            result?.query?.search?.forEachIndexed { index, search ->
                value = "query.search[$index]: $search"
            }
            value = "--- RESPONSE END ---"
        }
    }

    fun showError(message: String?) {
        with (_response) {
            value = "--- ERROR START ---"
            value = message
            value = "--- ERROR END ---"
        }
    }
}