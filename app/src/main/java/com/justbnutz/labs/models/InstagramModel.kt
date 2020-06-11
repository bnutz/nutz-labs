package com.justbnutz.labs.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IgModel(
    @Json(name = "sections")
    val sections: Any?,
    @Json(name = "global_blacklist_sample")
    val globalBlacklistSample: Any?,
    @Json(name = "users")
    val users: List<User?>?,
    @Json(name = "big_list")
    val bigList: Boolean?,
    @Json(name = "next_max_id")
    val nextMaxId: Any?,
    @Json(name = "page_size")
    val pageSize: Int?,
    @Json(name = "status")
    val status: String?
) {
    @JsonClass(generateAdapter = true)
    data class User(
        @Json(name = "pk")
        val pk: Long?,
        @Json(name = "username")
        val username: String?,
        @Json(name = "full_name")
        val fullName: String?,
        @Json(name = "is_private")
        val isPrivate: Boolean?,
        @Json(name = "profile_pic_url")
        val profilePicUrl: String?,
        @Json(name = "profile_pic_id")
        val profilePicId: String?,
        @Json(name = "is_verified")
        val isVerified: Boolean?,
        @Json(name = "has_anonymous_profile_picture")
        val hasAnonymousProfilePicture: Boolean?,
        @Json(name = "reel_auto_archive")
        val reelAutoArchive: String?,
        @Json(name = "latest_reel_media")
        val latestReelMedia: Int?
    )
}