package com.android.sample.data.local.objects

import androidx.compose.ui.graphics.toArgb
import com.android.sample.data.local.utils.InstantConverter
import com.android.sample.data.local.utils.encodeBooleanMap
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.theme.EventPalette
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.time.Instant

/**
 * Data class representing an Event entity for local storage using ObjectBox.
 *
 * @param objectId The unique ObjectBox ID for the entity.
 * @param id The unique identifier for the event.
 * @param organizationId The ID of the organization the event belongs to.
 * @param title The title of the event.
 * @param description The description of the event.
 * @param startDate The start date and time of the event.
 * @param endDate The end date and time of the event.
 * @param cloudStorageStatuses The cloud storage statuses associated with the event.
 * @param locallyStoredBy The identifiers of users who have the event stored locally.
 * @param personalNotes Any personal notes associated with the event.
 * @param participants The participants of the event.
 * @param version The version timestamp of the event.
 * @param hasBeenDeleted Flag indicating if the event has been deleted.
 * @param recurrenceStatus The recurrence status of the event.
 * @param color The color associated with the event.
 */
@Entity
data class EventEntity(
    @Id var objectId: Long = 0L,
    var id: String = "",
    var organizationId: String = "",
    var title: String = "",
    var description: String = "",
    @Convert(converter = InstantConverter::class, dbType = Long::class)
    var startDate: Instant = Instant.now(),
    @Convert(converter = InstantConverter::class, dbType = Long::class)
    var endDate: Instant = Instant.now(),
    var cloudStorageStatuses: String = "",
    var locallyStoredBy: String = "",
    var personalNotes: String? = null,
    var participants: String = "",
    var presence: String = encodeBooleanMap(emptyMap()),
    var version: Long = System.currentTimeMillis(),
    var hasBeenDeleted: Boolean = false,
    var recurrenceStatus: String = RecurrenceStatus.OneTime.name,
    var color: Int = EventPalette.Blue.toArgb()
)
