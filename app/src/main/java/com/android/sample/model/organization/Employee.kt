package com.android.sample.model.organization

import com.android.sample.model.authentication.User

data class Employee(
    val user: User = User(),
    val role: Role = Role.EMPLOYEE,
)
