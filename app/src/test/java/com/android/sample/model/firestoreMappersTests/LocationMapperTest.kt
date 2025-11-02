package com.android.sample.model.firestoreMappersTests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.firestoreMappers.LocationMapper
import com.android.sample.model.map.Location
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class LocationMapperTest {

  private lateinit var mockDocument: DocumentSnapshot
  private val sampleLocation = Location(latitude = 48.8566, longitude = 2.3522, label = "Paris")
  private val sampleMap = mapOf("latitude" to 48.8566, "longitude" to 2.3522, "label" to "Paris")

  @Before
  fun setUpTest() {
    mockDocument = mock(DocumentSnapshot::class.java)
  }

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidData_returnsLocation() = runBlocking {
    `when`(mockDocument.getDouble("latitude")).thenReturn(sampleLocation.latitude)
    `when`(mockDocument.getDouble("longitude")).thenReturn(sampleLocation.longitude)
    `when`(mockDocument.getString("label")).thenReturn(sampleLocation.label)

    val result = LocationMapper.fromDocument(mockDocument)

    assertNotNull(result)
    assertEquals(sampleLocation.latitude, result!!.latitude, 0.0)
    assertEquals(sampleLocation.longitude, result.longitude, 0.0)
    assertEquals(sampleLocation.label, result.label)
  }

  @Test
  fun fromDocument_missingLatitude_returnsNull() = runBlocking {
    `when`(mockDocument.getDouble("latitude")).thenReturn(null)
    `when`(mockDocument.getDouble("longitude")).thenReturn(sampleLocation.longitude)

    val result = LocationMapper.fromDocument(mockDocument)
    assertNull(result)
  }

  @Test
  fun fromDocument_missingLongitude_returnsNull() = runBlocking {
    `when`(mockDocument.getDouble("latitude")).thenReturn(sampleLocation.latitude)
    `when`(mockDocument.getDouble("longitude")).thenReturn(null)

    val result = LocationMapper.fromDocument(mockDocument)
    assertNull(result)
  }

  // --- fromMap tests ---
  @Test
  fun fromMap_withValidData_returnsLocation() {
    val result = LocationMapper.fromMap(sampleMap)
    assertNotNull(result)
    assertEquals(sampleLocation.latitude, result!!.latitude, 0.0)
    assertEquals(sampleLocation.longitude, result.longitude, 0.0)
    assertEquals(sampleLocation.label, result.label)
  }

  @Test
  fun fromMap_missingLatitude_returnsNull() {
    val invalidMap = sampleMap - "latitude"
    val result = LocationMapper.fromMap(invalidMap)
    assertNull(result)
  }

  @Test
  fun fromMap_missingLongitude_returnsNull() {
    val invalidMap = sampleMap - "longitude"
    val result = LocationMapper.fromMap(invalidMap)
    assertNull(result)
  }

  @Test
  fun fromMap_latitudeLongitudeAsInt_returnsLocation() {
    val intMap = mapOf("latitude" to 48, "longitude" to 2, "label" to "Paris")
    val result = LocationMapper.fromMap(intMap)
    assertNotNull(result)
    assertEquals(48.0, result!!.latitude, 0.0)
    assertEquals(2.0, result.longitude, 0.0)
  }

  // --- fromAny tests ---
  @Test
  fun fromAny_withDocument_returnsLocation() = runBlocking {
    `when`(mockDocument.getDouble("latitude")).thenReturn(sampleLocation.latitude)
    `when`(mockDocument.getDouble("longitude")).thenReturn(sampleLocation.longitude)
    `when`(mockDocument.getString("label")).thenReturn(sampleLocation.label)

    val result = LocationMapper.fromAny(mockDocument)
    assertNotNull(result)
    assertEquals(sampleLocation.latitude, result!!.latitude, 0.0)
    assertEquals(sampleLocation.longitude, result.longitude, 0.0)
    assertEquals(sampleLocation.label, result.label)
  }

  @Test
  fun fromAny_withMap_returnsLocation() {
    val result = LocationMapper.fromAny(sampleMap)
    assertNotNull(result)
    assertEquals(sampleLocation.latitude, result!!.latitude, 0.0)
    assertEquals(sampleLocation.longitude, result.longitude, 0.0)
    assertEquals(sampleLocation.label, result.label)
  }

  @Test
  fun fromAny_withInvalidType_returnsNull() {
    val result = LocationMapper.fromAny("not a location")
    assertNull(result)
  }

  // --- toMap tests ---
  @Test
  fun toMap_returnsCorrectMap() {
    val map = LocationMapper.toMap(sampleLocation)
    assertEquals(sampleLocation.latitude, map["latitude"])
    assertEquals(sampleLocation.longitude, map["longitude"])
    assertEquals(sampleLocation.label, map["label"])
  }
}
