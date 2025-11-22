package com.android.sample.model.firestoreMappers

import android.util.Log
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date

/** Maps Firestore documents to [Invitation] objects and vice versa. */
object InvitationMapper : FirestoreMapper<Invitation> {

  private const val ID_FIELD = "id"
  private const val ORGANIZATION_ID_FIELD = "organizationId"
  private const val CODE_FIELD = "code"
  private const val CREATED_AT_FIELD = "createdAt"
  private const val ACCEPTED_AT_FIELD = "acceptedAt"
  private const val INVITEE_EMAIL_FIELD = "inviteeEmail"
  private const val STATUS_FIELD = "status"

  override fun fromDocument(document: DocumentSnapshot): Invitation? {
    val id = document.getString(ID_FIELD) ?: return null
    val organizationId = document.getString(ORGANIZATION_ID_FIELD) ?: return null
    val code = document.getString(CODE_FIELD) ?: return null

    val createdAt = document.getTimestamp(CREATED_AT_FIELD)?.toDate()?.toInstant() ?: return null

    val acceptedAt = document.getTimestamp(ACCEPTED_AT_FIELD)?.toDate()?.toInstant()

    val inviteeEmail = document.getString(INVITEE_EMAIL_FIELD)

    val status =
        runCatching { InvitationStatus.valueOf(document.getString(STATUS_FIELD) ?: "Active") }
            .getOrDefault(InvitationStatus.Active)

    return Invitation(
        id = id,
        organizationId = organizationId,
        code = code,
        createdAt = createdAt,
        acceptedAt = acceptedAt,
        inviteeEmail = inviteeEmail,
        status = status)
  }

  override fun fromMap(data: Map<String, Any?>): Invitation? {
    val id = data[ID_FIELD] as? String ?: return null
    val organizationId = data[ORGANIZATION_ID_FIELD] as? String ?: return null
    val code = data[CODE_FIELD] as? String ?: return null

    val createdAt =
        (data[CREATED_AT_FIELD] as? Timestamp)?.toDate()?.toInstant()
            ?: (data[CREATED_AT_FIELD] as? Date)?.toInstant()
            ?: (data[CREATED_AT_FIELD] as? Long)?.let { Instant.ofEpochMilli(it) }
            ?: return null

    val acceptedAt =
        (data[ACCEPTED_AT_FIELD] as? Timestamp)?.toDate()?.toInstant()
            ?: (data[ACCEPTED_AT_FIELD] as? Date)?.toInstant()
            ?: (data[ACCEPTED_AT_FIELD] as? Long)?.let { Instant.ofEpochMilli(it) }

    val inviteeEmail = data[INVITEE_EMAIL_FIELD] as? String

    val statusString = data[STATUS_FIELD] as? String ?: return null
    val status =
        runCatching { InvitationStatus.valueOf(statusString) }
            .getOrElse {
              Log.e("InvitationMapper", "Unknown status \"$statusString\". By default : Inactive.")
              InvitationStatus.Expired
            }

    return Invitation(
        id = id,
        organizationId = organizationId,
        code = code,
        createdAt = createdAt,
        acceptedAt = acceptedAt,
        inviteeEmail = inviteeEmail,
        status = status)
  }

  override fun toMap(model: Invitation): Map<String, Any?> {
    return mapOf(
        ID_FIELD to model.id,
        ORGANIZATION_ID_FIELD to model.organizationId,
        CODE_FIELD to model.code,
        CREATED_AT_FIELD to Timestamp(Date.from(model.createdAt)),
        ACCEPTED_AT_FIELD to model.acceptedAt?.let { Timestamp(Date.from(it)) },
        INVITEE_EMAIL_FIELD to model.inviteeEmail,
        STATUS_FIELD to model.status.name)
  }
}
