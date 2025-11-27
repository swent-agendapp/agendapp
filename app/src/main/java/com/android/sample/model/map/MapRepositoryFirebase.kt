package com.android.sample.model.map

import com.android.sample.model.constants.FirestoreConstants.MAP_COLLECTION_PATH
import com.android.sample.model.constants.FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.AreaMapper
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.tasks.await

/** Represents a repository that manages a local list of markers and areas. */
class MapRepositoryFirebase(private val db: FirebaseFirestore) : MapRepository {

  // Helper properties to access markers and areas for the current organization
  private data class OrgData(
      val markers: ConcurrentHashMap<String, Marker> = ConcurrentHashMap(),
      val areas: ConcurrentHashMap<String, Area> = ConcurrentHashMap()
  )

  // In-memory storage for markers and areas by organization ID
  private val dataByOrganization = ConcurrentHashMap<String, OrgData>()

  // Helper function to get or create OrgData for a given organization ID
  private fun getOrCreate(orgId: String): OrgData = dataByOrganization.getOrPut(orgId) { OrgData() }

  // Helper function to get the Firestore collection reference for map data
  private fun mapDataCollection(orgId: String) =
      db.collection(ORGANIZATIONS_COLLECTION_PATH).document(orgId).collection(MAP_COLLECTION_PATH)

  private fun getNewUid(orgId: String): String {
    return mapDataCollection(orgId).document().id
  }

  override fun addMarker(orgId: String, marker: Marker) {
    getOrCreate(orgId).markers[marker.id] = marker
  }

  override fun removeMarker(orgId: String, id: String) {
    getOrCreate(orgId).markers.remove(id)
  }

  override fun getMarkerById(orgId: String, id: String): Marker? = getOrCreate(orgId).markers[id]

  override fun getAllMarkers(orgId: String): List<Marker> =
      getOrCreate(orgId).markers.values.toList()

  override fun getAllMarkersIds(orgId: String): List<String> =
      getOrCreate(orgId).markers.keys.toList()

  override fun getAllAreasIds(orgId: String): List<String> = getOrCreate(orgId).areas.keys.toList()

  override fun getAreaById(orgId: String, id: String): Area? = getOrCreate(orgId).areas[id]

  override suspend fun getAllAreas(orgId: String): List<Area> {
    val snapshot = mapDataCollection(orgId).get().await()
    return snapshot.mapNotNull { AreaMapper.fromDocument(it) }
  }

  override suspend fun createArea(orgId: String, label: String?, markerIds: List<String>) {
    val uid = getNewUid(orgId)
    val markers = markerIds.mapNotNull { getMarkerById(orgId, it) }

    val area = Area(uid, label, markers)
    val map = AreaMapper.toMap(area)

    mapDataCollection(orgId).document(uid).set(map).await()

    // Update local cache
    getOrCreate(orgId).areas[uid] = area
  }
}
