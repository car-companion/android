package com.dsd.carcompanion.api.models

import com.google.gson.annotations.SerializedName

data class TokenModel (
    @SerializedName("access")
    val Access: String,

    @SerializedName("refresh")
    val Refresh: String
)