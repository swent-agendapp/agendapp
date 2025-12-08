package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.category.EventCategory
import com.android.sample.model.firestoreMappers.EventCategoryMapper
import com.android.sample.ui.theme.EventPalette
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Test
import org.mockito.Mockito.*

// Assisted by AI

class EventCategoryMapperTest {

  private val sampleCategory =
      EventCategory(
          id = "category123",
          organizationId = "testOrg",
          label = "Test Category",
          color = EventPalette.Blue,
          isDefault = false,
      )

  private val sampleMap: Map<String, Any?> =
      mapOf(
          "id" to sampleCategory.id,
          "organizationId" to sampleCategory.organizationId,
          "label" to sampleCategory.label,
          "color" to sampleCategory.color.value.toLong(),
          "isDefault" to sampleCategory.isDefault,
      )

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidDocument_returnsCategory() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("category123")
    `when`(doc.getString("organizationId")).thenReturn("testOrg")
    `when`(doc.getString("label")).thenReturn("Test Category")
    `when`(doc.getLong("color")).thenReturn(sampleCategory.color.value.toLong())
    `when`(doc.getBoolean("isDefault")).thenReturn(false)

    val category = EventCategoryMapper.fromDocument(doc)

    assertThat(category).isNotNull()
    assertThat(category).isEqualTo(sampleCategory)
  }

  @Test
  fun fromDocument_missingOrganizationId_returnsNull() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("category123")
    `when`(doc.getString("organizationId")).thenReturn(null)

    val category = EventCategoryMapper.fromDocument(doc)

    assertThat(category).isNull()
  }

  // --- fromMap tests ---
  @Test
  fun fromMap_withValidData_returnsCategory() {
    val category = EventCategoryMapper.fromMap(sampleMap)

    assertThat(category).isNotNull()
    assertThat(category).isEqualTo(sampleCategory)
  }

  @Test
  fun fromMap_missingRequiredFields_returnsNull() {
    val invalidMap = sampleMap - "id"

    val category = EventCategoryMapper.fromMap(invalidMap)

    assertThat(category).isNull()
  }

  @Test
  fun fromMap_withMissingOptionalFields_usesDefaults() {
    val minimalMap =
        mapOf(
            "id" to "categoryMinimal",
            "organizationId" to "testOrg",
        )

    val category = EventCategoryMapper.fromMap(minimalMap)

    assertThat(category).isNotNull()
    assertThat(category!!.id).isEqualTo("categoryMinimal")
    assertThat(category.organizationId).isEqualTo("testOrg")
    assertThat(category.label).isEqualTo("Uncategorized")
    assertThat(category.color).isEqualTo(EventPalette.NoCategory)
    assertThat(category.isDefault).isTrue()
  }

  // --- fromAny tests ---
  @Test
  fun fromAny_withDocument_returnsCategory() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("category123")
    `when`(doc.getString("organizationId")).thenReturn("testOrg")
    `when`(doc.getString("label")).thenReturn("Test Category")
    `when`(doc.getLong("color")).thenReturn(sampleCategory.color.value.toLong())
    `when`(doc.getBoolean("isDefault")).thenReturn(false)

    val category = EventCategoryMapper.fromAny(doc)

    assertThat(category).isNotNull()
    assertThat(category!!.id).isEqualTo("category123")
  }

  @Test
  fun fromAny_withMap_returnsCategory() {
    val category = EventCategoryMapper.fromAny(sampleMap)

    assertThat(category).isNotNull()
    assertThat(category).isEqualTo(sampleCategory)
  }

  @Test
  fun fromAny_withInvalidType_returnsNull() {
    val category = EventCategoryMapper.fromAny("invalid")

    assertThat(category).isNull()
  }

  // --- toMap tests ---
  @Test
  fun toMap_returnsCorrectMap() {
    val map = EventCategoryMapper.toMap(sampleCategory)

    assertThat(map["id"]).isEqualTo(sampleCategory.id)
    assertThat(map["organizationId"]).isEqualTo(sampleCategory.organizationId)
    assertThat(map["label"]).isEqualTo(sampleCategory.label)
    assertThat(map["color"]).isEqualTo(sampleCategory.color.value.toLong())
    assertThat(map["isDefault"]).isEqualTo(sampleCategory.isDefault)
  }
}
