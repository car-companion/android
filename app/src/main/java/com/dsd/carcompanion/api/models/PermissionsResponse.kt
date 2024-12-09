package com.dsd.carcompanion.api.models

import com.google.gson.annotations.SerializedName


data class PermissionsResponse(
    val permissions: List<PermissionResponse>
)

data class PermissionResponse(
    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: PermissionType,

    @SerializedName("status")
    val status: Int
)

data class PermissionType(
    @SerializedName("name")
    val name: String
)
