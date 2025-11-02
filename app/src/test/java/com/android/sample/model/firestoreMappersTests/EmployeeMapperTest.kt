package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.firestoreMappers.EmployeeMapper
import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.Role
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class EmployeeMapperTest {

  @Test
  fun `fromDocument maps valid firestore doc to Employee`() {
    val doc = mock<DocumentSnapshot>()
    whenever(doc.id).thenReturn("uid_1")
    whenever(doc.getString("userId")).thenReturn("uid_1")
    whenever(doc.getString("displayName")).thenReturn("Alice")
    whenever(doc.getString("email")).thenReturn("alice@example.com")
    whenever(doc.getString("role")).thenReturn("ADMIN")

    val result = EmployeeMapper.fromDocument(doc)

    assertNotNull(result)
    assertEquals("uid_1", result!!.userId)
    assertEquals("Alice", result.displayName)
    assertEquals("alice@example.com", result.email)
    assertEquals(Role.ADMIN, result.role)
  }

  @Test
  fun `toMap produces all expected fields`() {
    val employee =
        Employee(
            userId = "uid_2", displayName = "Bob", email = "bob@example.com", role = Role.EMPLOYEE)

    val map = EmployeeMapper.toMap(employee)

    assertEquals("uid_2", map["userId"])
    assertEquals("Bob", map["displayName"])
    assertEquals("bob@example.com", map["email"])
    assertEquals("EMPLOYEE", map["role"])
    assertNotNull(map["updatedAt"] as? Timestamp)
  }
}
