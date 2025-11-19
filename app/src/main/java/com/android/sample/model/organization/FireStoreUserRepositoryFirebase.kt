package com.android.sample.model.organization

import com.android.sample.model.firestoreMappers.EmployeeMapper
import com.android.sample.model.versioning.withUpdatedVersion
import com.github.se.bootcamp.model.authentication.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    return snap.documents.mapNotNull { EmployeeMapper.fromDocument(it) }
  }

  override suspend fun newEmployee(employee: Employee) {
    require(employee.user.id.isNotBlank()) { "userId is required" }
    val updatedEmployee = employee.withUpdatedVersion()
    val data = EmployeeMapper.toMap(updatedEmployee)
    employeesCol().document(employee.user.id).set(data).await()
  }

  override suspend fun deleteEmployee(userId: String) {
    employeesCol().document(userId).delete().await()
  }

  override suspend fun getMyRole(): Role? {
    val uid = authRepository.getCurrentUser()?.id ?: return null
    val doc = employeesCol().document(uid).get().await()
    if (!doc.exists()) return null
    val employee = EmployeeMapper.fromDocument(doc) ?: return null
    return employee.role
  }
}
