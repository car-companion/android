package com.dsd.carcompanion.api.models

import com.google.gson.annotations.SerializedName

data class CreateUserRequest (
    @SerializedName("email")
    val Email: String,

    @SerializedName("username")
    val Username: String,

    @SerializedName("first_name")
    val FirstName: String,

    @SerializedName("last_name")
    val LastName: String,

    @SerializedName("password")
    val Password: String,
)