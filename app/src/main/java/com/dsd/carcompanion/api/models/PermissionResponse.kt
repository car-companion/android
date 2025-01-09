package com.dsd.carcompanion.api.models

data class GrantPermissionRequest(
    val permission_type: String,
    val valid_until: String
)

data class PermissionResponse (
    val component_type: String,
    val component_name: String,
    val permission_type: String,
    val valid_until: String
)

data class PermissionsResponse(
    val user: String,
    val permissions: List<PermissionResponse>
)

data class ComponentGranted(
    val component_type: String,
    val component_name: String,
    val status: String
)

data class ComponentRevoked(
    val component_type: String,
    val component_name: String,
    val permission_type: String
)

data class GrantedPermissions(
    val granted: List<ComponentGranted>,
    val failed: List<ComponentGranted>
)

data class RevokedPermissions(
    val revoked: List<ComponentRevoked>,
    val message: String
)

data class RemovedVehicle(
    val code: String,
    val message: String
)