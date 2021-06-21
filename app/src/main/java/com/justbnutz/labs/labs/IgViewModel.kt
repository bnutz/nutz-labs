package com.justbnutz.labs.labs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.justbnutz.labs.models.IgModel
import com.justbnutz.labs.services.IgApiService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Reference: https://siongui.github.io/2018/02/12/go-get-instagram-following-followers/
 */
class IgViewModel : BaseViewModel() {

    val DS_USER_ID = "ds_user_id"
    val SESSIONID = "sessionid"
    val CSRFTOKEN = "csrftoken"

    private val cookieList = mutableListOf<Cookie>()
    private val cookieJar = object : CookieJar {
        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieList

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {}
    }

    private val profileService by lazy { IgApiService.createP(cookieJar) }
    private val apiService by lazy { IgApiService.create(cookieJar) }

    private val _response by lazy { MutableLiveData<String>() }
    val response: LiveData<String>
        get() = _response

    fun updateCookies(cookies: List<Cookie>) {
        cookieList.clear()
        cookies.forEach {
            when (it.name) {
                DS_USER_ID,
                SESSIONID,
                CSRFTOKEN -> {
                    // Only add the bare minimum of properties
                    cookieList.add(
                        Cookie.Builder()
                            .domain(it.domain)
                            .name(it.name)
                            .value(it.value)
                            .build()
                    )
                }
            }
        }
    }

    private fun getUid(username: String): Observable<String> {
        return profileService.getProfilePage(username.trim())
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map {
                // Trim away the parts to get uid of username
                var page = it.string()
                val matchStart = "profilePage_"
                val indexStart = page.indexOf(matchStart)
                page = page.removeRange(0, indexStart + matchStart.length)
                val matchEnd = "\""
                val indexEnd = page.indexOf(matchEnd)
                page = page.removeRange(indexEnd, page.length)
                if (page.toLongOrNull() == null) throw Exception("Get UID failed")
                page
            }
    }

    fun getFollowers(username: String) = queryApi(username, false)

    fun getFollowing(username: String) = queryApi(username, true)

    private fun queryApi(username: String, getFollowing: Boolean) {
        getUid(username)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { userId ->
                    val responseList = mutableListOf<IgModel.User?>()
                    val baseUrl = "https://instagram.com/%s"

                    // Nested observable as we only want to repeat this block
                    var nextMaxId: Any? = null
                    var index = 0
                    Observable.just(userId)
                        .subscribeOn(Schedulers.io())
                        .delay(Random.nextLong(2, 6), TimeUnit.SECONDS)
                        .flatMap {
                            if (getFollowing) apiService.getFollowing(userId, nextMaxId)
                            else apiService.getFollowers(userId, nextMaxId)
                        }
                        .map {
                            nextMaxId = it.nextMaxId
                            it.users?.let { userList -> responseList.addAll(userList) }
                            it
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .map {
                            it.users?.forEach { user ->
                                index = index.inc()
                                _response.value = "[$index]\t${user?.pk}\t${String.format(baseUrl, user?.username)}\t${user?.fullName}\t${user?.isVerified}\t${user?.isPrivate}\t${user?.profilePicUrl}"
                            }
                            it
                        }
                        .repeat()
                        .takeWhile { it.bigList == true }
                        .subscribeBy(
                            onError = { showError(it) }
                        )
                        .addTo(compositeDisposable)
                    // Not using "onNext" in subscribe block as it only runs when repeating
                },
                onError = { showError(it) }
            )
            .addTo(compositeDisposable)
    }

    fun showError(error: Throwable?) {
        with (_response) {
            value = "--- ERROR START ---"
            value = "${error?.message}\n${(error as? HttpException)?.response()?.errorBody()?.string()}"
            value = "--- ERROR END ---"
        }
    }
}