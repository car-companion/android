package com.dsd.carcompanion.api.models

import com.google.gson.annotations.SerializedName

data class CreateUserRequest (
    @SerializedName("email")
    var Email: String,

    @SerializedName("username")
    var Username: String,

    @SerializedName("first_name")
    var FirstName: String,

    @SerializedName("last_name")
    var LastName: String,

    @SerializedName("password")
    var Password: String,
)