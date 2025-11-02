package com.android.sample.model.organization

import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import org.junit.Test

class EmployeeRepositoryFirebaseTest {

  @Test
  fun toFirestore_maps_all_fields() {
    val emp = Employee("u1", "Alice", "alice@x.com", Role.ADMIN)

    val dto = emp.toFirestore()

    assertThat(dto.userId).isEqualTo("u1")
    assertThat(dto.displayName).isEqualTo("Alice")
    assertThat(dto.email).isEqualTo("alice@x.com")
    assertThat(dto.role).isEqualTo("ADMIN")
    assertThat(dto.updatedAt).isInstanceOf(Timestamp::class.java)
  }

  @Test
  fun toDomain_parses_valid_role() {
    val dto = FirestoreEmployee("u1", "Bob", "b@x.com", role = "EMPLOYEE")

    val emp = dto.toDomain()

    assertThat(emp.role).isEqualTo(Role.EMPLOYEE)
  }

  @Test
  fun toDomain_returns_EMPLOYEE_on_invalid_role() {
    val dto = FirestoreEmployee("u1", "Bob", "b@x.com", role = "WHATEVER")

    val emp = dto.toDomain()

    assertThat(emp.role).isEqualTo(Role.EMPLOYEE)
  }
}
