package com.dsd.carcompanion.api.models

import com.google.gson.annotations.SerializedName

data class UserModel (
    @SerializedName("id")
    val Id: Int,

    @SerializedName("email")
    val Email: String,

    @SerializedName("username")
    val Username: String
    )