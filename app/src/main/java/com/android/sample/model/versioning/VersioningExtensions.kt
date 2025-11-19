package com.android.sample.model.versioning

import com.android.sample.model.authentication.User
import com.android.sample.model.map.Area
import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.Organization

private fun currentVersionTimestamp(): Long = System.currentTimeMillis()

/** Returns a copy of the [User] with an updated version timestamp. */
fun User.withUpdatedVersion(): User = copy(version = currentVersionTimestamp())

/** Returns a copy of the [Area] with an updated version timestamp. */
fun Area.withUpdatedVersion(): Area = copy(version = currentVersionTimestamp())

/** Returns a copy of the [Employee] with updated employee and nested user versions. */
fun Employee.withUpdatedVersion(): Employee =
    copy(version = currentVersionTimestamp(), user = user.withUpdatedVersion())

/** Returns a copy of the [Organization] with a refreshed version and nested members. */
fun Organization.withUpdatedVersion(): Organization =
    copy(
        version = currentVersionTimestamp(),
        admins = admins.map { it.withUpdatedVersion() },
        members = members.map { it.withUpdatedVersion() },
        areas = areas.map { it.withUpdatedVersion() },
    )
