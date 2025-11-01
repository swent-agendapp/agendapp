package com.android.sample.model.organization

data class Employee(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: Role = Role.EMPLOYEE,
)
