package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.authentication.User
import com.android.sample.model.firestoreMappers.UserMapper
import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class UserMapperTest {

  private val sampleUser = User("user123", "John Doe", "john@example.com")
  private val sampleMap =
      mapOf("id" to "user123", "displayName" to "John Doe", "email" to "john@example.com")

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidDocument_returnsUser() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn(sampleUser.id)
    `when`(doc.getString("displayName")).thenReturn(sampleUser.displayName)
    `when`(doc.getString("email")).thenReturn(sampleUser.email)
    `when`(doc.id).thenReturn("fallbackId")

    val user = UserMapper.fromDocument(doc)
    assertNotNull(user)
    assertEquals(sampleUser.id, user!!.id)
    assertEquals(sampleUser.displayName, user.displayName)
    assertEquals(sampleUser.email, user.email)
  }

  @Test
  fun fromDocument_missingDisplayName_stillCreatesUser() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn(sampleUser.id)
    `when`(doc.getString("displayName")).thenReturn(null)
    `when`(doc.getString("email")).thenReturn(sampleUser.email)
    `when`(doc.id).thenReturn("fallbackId")

    val user = UserMapper.fromDocument(doc)

    assertNotNull(user)
    assertEquals(sampleUser.id, user!!.id)
    assertNull(user.displayName)
    assertEquals(sampleUser.email, user.email)
  }

  @Test
  fun fromDocument_missingEmail_stillCreatesUser() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn(sampleUser.id)
    `when`(doc.getString("displayName")).thenReturn(sampleUser.displayName)
    `when`(doc.getString("email")).thenReturn(null)
    `when`(doc.id).thenReturn("fallbackId")

    val user = UserMapper.fromDocument(doc)

    assertNotNull(user)
    assertEquals(sampleUser.id, user!!.id)
    assertEquals(sampleUser.displayName, user.displayName)
    assertNull(user.email)
  }

  @Test
  fun fromDocument_missingId_usesDocumentId() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn(null)
    `when`(doc.getString("displayName")).thenReturn(sampleUser.displayName)
    `when`(doc.getString("email")).thenReturn(sampleUser.email)
    `when`(doc.id).thenReturn("fallbackId")

    val user = UserMapper.fromDocument(doc)
    assertNotNull(user)
    assertEquals("fallbackId", user!!.id)
  }

  // --- fromMap tests ---
  @Test
  fun fromMap_withValidData_returnsUser() {
    val user = UserMapper.fromMap(sampleMap)
    assertNotNull(user)
    assertEquals(sampleUser, user)
  }

  @Test
  fun fromMap_missingId_returnsNull() {
    val invalidMap = sampleMap - "id"
    val user = UserMapper.fromMap(invalidMap)
    assertNull(user)
  }

  // --- fromAny tests ---
  @Test
  fun fromAny_withDocument_returnsUser() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn(sampleUser.id)
    `when`(doc.getString("displayName")).thenReturn(sampleUser.displayName)
    `when`(doc.getString("email")).thenReturn(sampleUser.email)
    `when`(doc.id).thenReturn("fallbackId")

    val user = UserMapper.fromAny(doc)
    assertNotNull(user)
    assertEquals(sampleUser, user)
  }

  @Test
  fun fromAny_withMap_returnsUser() {
    val user = UserMapper.fromAny(sampleMap)
    assertNotNull(user)
    assertEquals(sampleUser, user)
  }

  @Test
  fun fromAny_withInvalidType_returnsNull() {
    val user = UserMapper.fromAny("not a user")
    assertNull(user)
  }

  // --- toMap tests ---
  @Test
  fun toMap_returnsCorrectMap() {
    val map = UserMapper.toMap(sampleUser)
    assertEquals(4, map.size)
    assertEquals(sampleUser.id, map["id"])
    assertEquals(sampleUser.displayName, map["displayName"])
    assertEquals(sampleUser.email, map["email"])
    assertEquals(sampleUser.phoneNumber, map["phoneNumber"])
  }
}
