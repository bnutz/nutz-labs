package com.justbnutz.labs.services

import com.justbnutz.labs.models.IgModel
import io.reactivex.Observable
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class IgApiService {

    interface IIgProfileService {
        @GET("{username}")
        fun getProfilePage(
            @Path("username") username: String
        ): Observable<ResponseBody>
    }

    interface IIgApiService {
        @GET("users/{userId}/info/")
        fun getInfo(
            @Path("userId") userId: String
        ): Observable<ResponseBody>

        @GET("friendships/{userId}/followers/")
        fun getFollowers(
            @Path("userId") userId: String,
            @Query("max_id") maxId: Any? = null
        ): Observable<IgModel>

        @GET("friendships/{userId}/following/")
        fun getFollowing(
            @Path("userId") userId: String,
            @Query("max_id") maxId: Any? = null
        ): Observable<IgModel>
    }

    companion object {
        private const val USER_AGENT = "Instagram 10.3.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+"

        fun createP(cookieJar: CookieJar): IIgProfileService {
            return ApiFactory
                .build(
                    "https://instagram.com",
                    useCookieJar = cookieJar
                )
                .create(IIgProfileService::class.java)
        }

        fun create(cookieJar: CookieJar): IIgApiService {
            val uaInterceptor = object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .build()
                    return chain.proceed(request)
                }
            }

            return ApiFactory
                .build(
                    "https://i.instagram.com/api/v1/",
                    addInterceptors = listOf(uaInterceptor),
                    useCookieJar = cookieJar
                )
                .create(IIgApiService::class.java)
        }
    }
}