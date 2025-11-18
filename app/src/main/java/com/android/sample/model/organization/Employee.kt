package com.android.sample.model.organization

import com.android.sample.model.authentication.User

data class Employee(
    val user: User = User(displayName = "", email = ""),
    val role: Role = Role.EMPLOYEE,
)
