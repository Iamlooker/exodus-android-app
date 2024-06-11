package org.eu.exodus_privacy.exodusprivacy.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Trackers(
    val trackers: Map<String, Tracker> = emptyMap(),
)
