package com.android.sample.model.organization

import com.github.se.bootcamp.model.authentication.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

data class FirestoreEmployee(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = Role.EMPLOYEE.name,
    val updatedAt: Timestamp? = null,
)

fun Employee.toFirestore(): FirestoreEmployee =
    FirestoreEmployee(
        userId = userId,
        displayName = displayName,
        email = email,
        role = role.name,
        updatedAt = Timestamp.now())

fun FirestoreEmployee.toDomain(): Employee =
    Employee(
        userId = userId,
        displayName = displayName,
        email = email,
        role = runCatching { Role.valueOf(role) }.getOrDefault(Role.EMPLOYEE))

/**
 * Firebase implementation using a global collection: /employees/{userId}
 *
 * Document fields : { "userId": "firebaseUid", "displayName": "blablabla", "email":
 * "abc@exemple.com", "role": "ADMIN" | "EMPLOYEE", "updatedAt": <timestamp> }
 */
class EmployeeRepositoryFirebase(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository
) : EmployeeRepository {

  private fun employeesCol() = db.collection("employees")

  override suspend fun getEmployees(): List<Employee> {
    val snap = employeesCol().get().await()
    return snap.documents.mapNotNull { it.toObject<Employee>() }
  }

  override suspend fun newEmployee(employee: Employee) {
    require(employee.userId.isNotBlank()) { "userId is required" }
    employeesCol().document(employee.userId).set(employee.toFirestore()).await()
  }

  override suspend fun deleteEmployee(userId: String) {
    employeesCol().document(userId).delete().await()
  }

  override suspend fun getMyRole(): Role? {
    val uid = authRepository.getCurrentUser()?.id ?: return null
    val doc = employeesCol().document(uid).get().await()
    if (!doc.exists()) return null
    val roleName = doc.getString("role") ?: return null
    return runCatching { Role.valueOf(roleName) }.getOrNull()
  }
}
