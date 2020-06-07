package com.justbnutz.labs.services

import com.justbnutz.labs.models.WikiModel
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

class WikipediaApiService {

    interface IWikiApiService {
        @GET("api.php")
        fun hitCountCheck(
            @Query("action") action: String,
            @Query("format") format: String,
            @Query("list") list: String,
            @Query("srsearch") srsearch: String
        ): Observable<WikiModel>
    }

    companion object {
        fun create(): IWikiApiService {
            return ApiFactory
                .build("https://en.wikipedia.org/w/")
                .create(IWikiApiService::class.java)
        }
    }
}