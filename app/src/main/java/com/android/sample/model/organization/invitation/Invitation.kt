package com.android.sample.model.organization.invitation

import java.time.Instant
import java.util.UUID

data class Invitation(
    val id: String,
    val organizationId: String,
    val code: String,
    val createdAt: Instant = Instant.now(),
    val acceptedAt: Instant? = null,
    val inviteeEmail: String? = null,
    val status: InvitationStatus = InvitationStatus.Active
) {
  companion object {

    private const val CODE_LENGTH = 6
    private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    fun generateRandomCode(length: Int = CODE_LENGTH): String =
        (1..length).map { CHARS.random() }.joinToString("")

    fun create(
        organizationId: String,
    ): Invitation {
      return Invitation(
          id = UUID.randomUUID().toString(),
          organizationId = organizationId,
          code = generateRandomCode(),
          createdAt = Instant.now(),
          acceptedAt = null,
          inviteeEmail = null,
          status = InvitationStatus.Active)
    }
  }
}

enum class InvitationStatus {
  Active,
  Used,
  Expired,
}
