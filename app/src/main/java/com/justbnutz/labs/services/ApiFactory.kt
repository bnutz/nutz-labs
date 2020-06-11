package com.justbnutz.labs.services

import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiFactory {

    fun build(
        baseUrl: String,
        addInterceptors: List<Interceptor>? = null,
        addNetworkInterceptors: List<Interceptor>? = null,
        useCookieJar: CookieJar? = null): Retrofit {

        val interceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .apply {
                addInterceptors?.forEach { this.addInterceptor(it) }
                addNetworkInterceptors?.forEach { this.addNetworkInterceptor(it) }
                useCookieJar?.let { this.cookieJar(it) }
            }
            .build()

        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }
}