package com.justbnutz.labs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WikiModel (
    val batchcomplete : String?,
    @Json(name = "continue")
    val cont : Continue?,
    val query : Query?
)

@JsonClass(generateAdapter = true)
data class Continue (
    val sroffset : Int?,
    @Json(name = "continue")
    val cont : String?
)

@JsonClass(generateAdapter = true)
data class Query (
    val searchinfo : Searchinfo?,
    val search : List<Search>?
)

@JsonClass(generateAdapter = true)
data class Searchinfo (
    val totalhits : Int?
)

@JsonClass(generateAdapter = true)
data class Search (
    val ns : Int?,
    val title : String?,
    val pageid : Int?,
    val size : Int?,
    val wordcount : Int?,
    val snippet : String?,
    val timestamp : String?
)