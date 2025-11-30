package com.android.sample.model.organization.invitation

import java.time.Instant
import java.util.UUID

/**
 * Represents an invitation sent to a user to join an organization.
 *
 * Each invitation contains a unique identifier, a reference to the organization, a randomly
 * generated invitation code, timestamps for creation and acceptance, and metadata such as the
 * invitee's email and current status.
 *
 * @property id Unique identifier for this invitation.
 * @property organizationId The ID of the organization this invitation belongs to.
 * @property code Randomly generated alphanumeric invitation code.
 * @property createdAt Timestamp indicating when the invitation was created.
 * @property acceptedAt Timestamp indicating when the invitation was accepted, or `null` if it has
 *   not been accepted.
 * @property inviteeEmail Optional email of the invited user.
 * @property status Current status of the invitation (Active, Used, Expired).
 */
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

    /**
     * Generates a random alphanumeric invitation code.
     *
     * @param length Length of the generated code (defaults to [CODE_LENGTH]).
     * @return A randomly generated uppercase alphanumeric string.
     */
    fun generateRandomCode(length: Int = CODE_LENGTH): String =
        (1..length).map { CHARS.random() }.joinToString("")

    /**
     * Creates a new [Invitation] with a fresh UUID, random code, and default values.
     *
     * The invitation starts in the [InvitationStatus.Active] state and has no invitee email or
     * acceptance timestamp.
     *
     * @param organizationId The ID of the organization for which the invitation is created.
     * @return A new [Invitation] instance with generated defaults.
     */
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

/**
 * Represents the lifecycle status of an invitation.
 * - [Active]: The invitation can still be used.
 * - [Used]: The invitation has been consumed by the invitee.
 * - [Expired]: The invitation is no longer valid.
 */
enum class InvitationStatus {
  Active,
  Used,
  Expired,
}
