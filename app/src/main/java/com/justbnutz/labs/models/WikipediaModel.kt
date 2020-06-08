package com.justbnutz.labs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WikiModel(
    @Json(name = "batchcomplete")
    val batchcomplete: String?,
    @Json(name = "continue")
    val cont: Continue?,
    @Json(name = "query")
    val query: Query?
) {
    @JsonClass(generateAdapter = true)
    data class Continue(
        @Json(name = "sroffset")
        val sroffset: Int?,
        @Json(name = "continue")
        val cont: String?
    )

    @JsonClass(generateAdapter = true)
    data class Query(
        @Json(name = "searchinfo")
        val searchinfo: Searchinfo?,
        @Json(name = "search")
        val search: List<Search>?
    ) {
        @JsonClass(generateAdapter = true)
        data class Searchinfo(
            @Json(name = "totalhits")
            val totalhits: Int?,
            @Json(name = "suggestion")
            val suggestion: String?,
            @Json(name = "suggestionsnippet")
            val suggestionsnippet: String?
        )

        @JsonClass(generateAdapter = true)
        data class Search(
            @Json(name = "ns")
            val ns: Int?,
            @Json(name = "title")
            val title: String?,
            @Json(name = "pageid")
            val pageid: Int?,
            @Json(name = "size")
            val size: Int?,
            @Json(name = "wordcount")
            val wordcount: Int?,
            @Json(name = "snippet")
            val snippet: String?,
            @Json(name = "timestamp")
            val timestamp: String?
        )
    }
}