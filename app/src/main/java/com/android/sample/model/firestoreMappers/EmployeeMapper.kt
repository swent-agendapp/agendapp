package com.android.sample.model.firestoreMappers

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.Role
import com.google.firebase.firestore.DocumentSnapshot

/** Maps Firestore documents to [Employee] objects and vice versa. */
object EmployeeMapper : FirestoreMapper<Employee> {

  override fun fromDocument(document: DocumentSnapshot): Employee? {
    val userId = document.getString("userId") ?: document.id
    val displayName = document.getString("displayName") ?: ""
    val email = document.getString("email") ?: ""
    val phoneNumber = document.getString("phoneNumber")
    val roleName = document.getString("role") ?: return null
    val userVersion = document.getLong("userVersion") ?: 0L
    val employeeVersion = document.getLong("version") ?: 0L

    val role = runCatching { Role.valueOf(roleName) }.getOrNull() ?: return null

    return Employee(
        User(userId, displayName, email, phoneNumber = phoneNumber, version = userVersion),
        role,
        version = employeeVersion)
  }

  override fun fromMap(data: Map<String, Any?>): Employee? {
    val userId = data["userId"] as? String ?: return null
    val displayName = data["displayName"] as? String ?: ""
    val email = data["email"] as? String ?: ""
    val roleName = data["role"] as? String ?: return null
    val phoneNumber = data["phoneNumber"] as? String
    val userVersion = (data["userVersion"] as? Number)?.toLong() ?: 0L
    val employeeVersion = (data["version"] as? Number)?.toLong() ?: 0L

    val role = runCatching { Role.valueOf(roleName) }.getOrNull() ?: return null

    return Employee(
        User(userId, displayName, email, phoneNumber = phoneNumber, version = userVersion),
        role,
        version = employeeVersion)
  }

  override fun toMap(model: Employee): Map<String, Any?> {
    return mapOf(
        "userId" to model.user.id,
        "displayName" to model.user.displayName,
        "email" to model.user.email,
        "phoneNumber" to model.user.phoneNumber,
        "role" to model.role.name,
        "version" to model.version,
        "userVersion" to model.user.version)
  }
}
