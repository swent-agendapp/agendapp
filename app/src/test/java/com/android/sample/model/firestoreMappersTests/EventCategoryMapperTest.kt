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
          EventCategoryMapper.ID_FIELD to sampleCategory.id,
          EventCategoryMapper.ORGANIZATION_ID_FIELD to sampleCategory.organizationId,
          EventCategoryMapper.LABEL_FIELD to sampleCategory.label,
          EventCategoryMapper.COLOR_FIELD to sampleCategory.color.value.toLong(),
          EventCategoryMapper.IS_DEFAULT_FIELD to sampleCategory.isDefault,
      )

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidDocument_returnsCategory() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("category123")
    `when`(doc.getString(EventCategoryMapper.ORGANIZATION_ID_FIELD)).thenReturn("testOrg")
    `when`(doc.getString(EventCategoryMapper.LABEL_FIELD)).thenReturn("Test Category")
    `when`(doc.getLong(EventCategoryMapper.COLOR_FIELD))
        .thenReturn(sampleCategory.color.value.toLong())
    `when`(doc.getBoolean(EventCategoryMapper.IS_DEFAULT_FIELD)).thenReturn(false)

    val category = EventCategoryMapper.fromDocument(doc)

    assertThat(category).isNotNull()
    assertThat(category).isEqualTo(sampleCategory)
  }

  @Test
  fun fromDocument_missingOrganizationId_returnsNull() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("category123")
    `when`(doc.getString(EventCategoryMapper.ORGANIZATION_ID_FIELD)).thenReturn(null)

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
            EventCategoryMapper.ID_FIELD to "categoryMinimal",
            EventCategoryMapper.ORGANIZATION_ID_FIELD to "testOrg",
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
    `when`(doc.getString(EventCategoryMapper.ORGANIZATION_ID_FIELD)).thenReturn("testOrg")
    `when`(doc.getString(EventCategoryMapper.LABEL_FIELD)).thenReturn("Test Category")
    `when`(doc.getLong(EventCategoryMapper.COLOR_FIELD))
        .thenReturn(sampleCategory.color.value.toLong())
    `when`(doc.getBoolean(EventCategoryMapper.IS_DEFAULT_FIELD)).thenReturn(false)

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

    assertThat(map[EventCategoryMapper.ID_FIELD]).isEqualTo(sampleCategory.id)
    assertThat(map[EventCategoryMapper.ORGANIZATION_ID_FIELD])
        .isEqualTo(sampleCategory.organizationId)
    assertThat(map[EventCategoryMapper.LABEL_FIELD]).isEqualTo(sampleCategory.label)
    assertThat(map[EventCategoryMapper.COLOR_FIELD]).isEqualTo(sampleCategory.color.value.toLong())
    assertThat(map[EventCategoryMapper.IS_DEFAULT_FIELD]).isEqualTo(sampleCategory.isDefault)
  }
}
