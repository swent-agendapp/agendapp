package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.authentication.User
import com.android.sample.model.firestoreMappers.EmployeeMapper
import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.Role
import com.google.common.truth.Truth.assertThat
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
    whenever(doc.getString("phoneNumber")).thenReturn("123")
    whenever(doc.getLong("version")).thenReturn(22L)
    whenever(doc.getLong("userVersion")).thenReturn(11L)

    val result = EmployeeMapper.fromDocument(doc)

    assertNotNull(result)
    assertEquals("uid_1", result!!.user.id)
    assertEquals("Alice", result.user.displayName)
    assertEquals("alice@example.com", result.user.email)
    assertEquals("123", result.user.phoneNumber)
    assertEquals(Role.ADMIN, result.role)
    assertEquals(11L, result.user.version)
    assertEquals(22L, result.version)
  }

  @Test
  fun `toMap produces all expected fields`() {
    val employee =
        Employee(
            User("uid_2", "Bob", "bob@example.com", phoneNumber = "999", version = 7L),
            Role.EMPLOYEE,
            version = 17L)

    val map = EmployeeMapper.toMap(employee)

    assertEquals("uid_2", map["userId"])
    assertEquals("Bob", map["displayName"])
    assertEquals("bob@example.com", map["email"])
    assertEquals("999", map["phoneNumber"])
    assertEquals("EMPLOYEE", map["role"])
    assertEquals(17L, map["version"])
    assertEquals(7L, map["userVersion"])
  }

  @Test
  fun toMap_writes_all_fields() {
    val employee =
        Employee(
            user = User("u1", "Alice", "alice@x.com", phoneNumber = "777", version = 9L),
            role = Role.ADMIN,
            version = 19L,
        )

    val map = EmployeeMapper.toMap(employee)

    assertThat(map["userId"]).isEqualTo("u1")
    assertThat(map["displayName"]).isEqualTo("Alice")
    assertThat(map["email"]).isEqualTo("alice@x.com")
    assertThat(map["phoneNumber"]).isEqualTo("777")
    assertThat(map["role"]).isEqualTo("ADMIN")
    assertThat(map["version"]).isEqualTo(19L)
    assertThat(map["userVersion"]).isEqualTo(9L)
  }

  @Test
  fun fromMap_reads_valid_role() {
    val data =
        mapOf(
            "userId" to "u2",
            "displayName" to "Bob",
            "email" to "b@x.com",
            "role" to "EMPLOYEE",
            "version" to 30L,
            "userVersion" to 5L,
            "phoneNumber" to "555",
        )

    val employee = EmployeeMapper.fromMap(data)

    assertThat(employee).isNotNull()
    assertThat(employee!!.user.id).isEqualTo("u2")
    assertThat(employee.role).isEqualTo(Role.EMPLOYEE)
    assertThat(employee.user.version).isEqualTo(5L)
    assertThat(employee.version).isEqualTo(30L)
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
