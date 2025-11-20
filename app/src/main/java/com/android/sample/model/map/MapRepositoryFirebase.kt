package com.android.sample.model.map

import com.android.sample.model.calendar.Event
import com.android.sample.model.constants.FirestoreConstants.EVENTS_COLLECTION_PATH
import com.android.sample.model.constants.FirestoreConstants.MAP_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.AreaMapper
import com.android.sample.model.firestoreMappers.EventMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Represents a repository that manages a local list of markers and areas. */
class MapRepositoryFirebase(private val db: FirebaseFirestore) : MapRepository {
  private fun getNewUid(): String {
    return db.collection(MAP_COLLECTION_PATH).document().id
  }
  // In-memory storage for markers (keyed by Marker.id).
  private val markers = mutableMapOf<String, Marker>()

  // In-memory storage for areas (keyed by Area.id).

  override fun addMarker(marker: Marker) {
    markers[marker.id] = marker
  }

  override fun removeMarker(id: String) {
    markers.remove(key = id)
  }

  override fun getMarkerById(id: String): Marker? = markers[id]

  override fun getAllMarkers(): List<Marker> = markers.values.toList()

  override fun getAllMarkersIds(): List<String> = markers.keys.toList()



  override fun getAllAreasIds(): List<String> = throw NotImplementedError()

  override fun getAreaById(id: String): Area? = throw NotImplementedError()

  override suspend fun getAllAreas(): List<Area> {
    val snapshot = db.collection(MAP_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { AreaMapper.fromDocument(document = it) }
  }

  override suspend fun createArea(label: String?, markerIds: List<String>) {
    val uid = getNewUid()
    db.collection(MAP_COLLECTION_PATH)
      .document(uid)
      .set(AreaMapper.toMap(model = Area(uid, label, markerIds.mapNotNull { getMarkerById(it) })))
      .await()
  }
}
