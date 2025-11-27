package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.authentication.User
import com.android.sample.model.firestoreMappers.EmployeeMapper
import com.android.sample.model.organization.data.Employee
import com.android.sample.model.organization.data.Role
import com.google.common.truth.Truth.assertThat
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
    assertEquals("uid_1", result!!.user.id)
    assertEquals("Alice", result.user.displayName)
    assertEquals("alice@example.com", result.user.email)
    assertEquals(Role.ADMIN, result.role)
  }

  @Test
  fun `toMap produces all expected fields`() {
    val employee = Employee(User("uid_2", "Bob", "bob@example.com"), Role.EMPLOYEE)

    val map = EmployeeMapper.toMap(employee)

    assertEquals("uid_2", map["userId"])
    assertEquals("Bob", map["displayName"])
    assertEquals("bob@example.com", map["email"])
    assertEquals("EMPLOYEE", map["role"])
    assertNotNull(map["updatedAt"] as? Timestamp)
  }

  @Test
  fun toMap_writes_all_fields() {
    val employee =
        Employee(
            user = User("u1", "Alice", "alice@x.com"),
            role = Role.ADMIN,
        )

    val map = EmployeeMapper.toMap(employee)

    assertThat(map["userId"]).isEqualTo("u1")
    assertThat(map["displayName"]).isEqualTo("Alice")
    assertThat(map["email"]).isEqualTo("alice@x.com")
    assertThat(map["role"]).isEqualTo("ADMIN")
    assertThat(map["updatedAt"]).isInstanceOf(Timestamp::class.java)
  }

  @Test
  fun fromMap_reads_valid_role() {
    val data =
        mapOf(
            "userId" to "u2",
            "displayName" to "Bob",
            "email" to "b@x.com",
            "role" to "EMPLOYEE",
        )

    val employee = EmployeeMapper.fromMap(data)

    assertThat(employee).isNotNull()
    assertThat(employee!!.user.id).isEqualTo("u2")
    assertThat(employee.role).isEqualTo(Role.EMPLOYEE)
  }

  @Test
  fun fromDocument_returns_null_when_role_invalid() {
    val doc =
        mock<DocumentSnapshot> {
          whenever(it.id).thenReturn("u3")
          whenever(it.getString("displayName")).thenReturn("Charlie")
          whenever(it.getString("email")).thenReturn("c@x.com")
          whenever(it.getString("role")).thenReturn("INVALID_ROLE")
        }

    val employee = EmployeeMapper.fromDocument(doc)

    assertThat(employee).isNull()
  }
}
