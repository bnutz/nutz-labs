package com.justbnutz.labs.labs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import okhttp3.Cookie

class CookieViewModel : BaseViewModel() {

    private val _cookieList by lazy { MutableLiveData<List<Cookie>>() }
    val cookieList: LiveData<List<Cookie>>
        get() = _cookieList

    fun loadCookie(cookieText: String) {
        Observable.just(cookieText)
            .subscribeOn(Schedulers.computation())
            .map { checkText ->
                val buildList = mutableListOf<Cookie>()
                checkText.split("\n")
                    .filter { it.isNotBlank() && it.startsWith("#").not() }
                    .forEach { cookieBite ->
                        cookieBite.split("\t")
                            .takeIf { it.size >= 7 }?.let { cookieBit ->
                                buildList.add(
                                    makeCookie(
                                        cookieBit[0].removePrefix("."),
                                        cookieBit[1],
                                        cookieBit[2],
                                        cookieBit[3],
                                        cookieBit[4].toLong(),
                                        cookieBit[5],
                                        cookieBit[6]
                                    )
                                )
                            }
                    }
                buildList
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _cookieList.value = it.toList()
            }
            .addTo(compositeDisposable)
    }

    private fun makeCookie(
        domain: String,
        incSubdomain: String,
        path: String,
        secure: String,
        expires: Long,
        name: String,
        value: String): Cookie {

        val builder = Cookie.Builder()
            .path(path)
            .expiresAt(expires)
            .name(name)
            .value(value)

        if (incSubdomain.equals("TRUE", true)) builder.domain(domain)
        else builder.hostOnlyDomain(domain)

        if (secure.equals("TRUE", true)) builder.secure()

        return builder.build()
    }
}