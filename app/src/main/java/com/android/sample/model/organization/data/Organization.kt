package com.android.sample.model.organization.data

import com.android.sample.model.authentication.User
import com.android.sample.model.calendar.Event
import com.android.sample.model.category.EventCategory
import com.android.sample.model.map.Area
import java.util.UUID

/**
 * Represents an organization.
 *
 * @property id Unique identifier for the organization. By default a random UUID string is
 *   generated.
 * @property name Human-readable name of the organization.
 * @property admins List of users with administrative privileges in the organization.
 * @property members List of users who are members of the organization (may include admins).
 * @property areas List of geographic areas associated with the organization (used for
 *   geofencing/geo checks).
 * @property geoCheckEnabled Flag indicating whether geographic checks (e.g., entrance/exit
 *   detection) are enabled for this organization.
 */
data class Organization(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val admins: List<User> = emptyList(),
    val members: List<User> = emptyList(),
    val events: List<Event> = emptyList(),
    val categories: List<EventCategory> = emptyList(),
    val areas: List<Area> = emptyList(),
    val geoCheckEnabled: Boolean = false,
)
