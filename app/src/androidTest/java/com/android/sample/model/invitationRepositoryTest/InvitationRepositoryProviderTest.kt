package com.android.sample.model.invitationRepositoryTest

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationRepository
import com.android.sample.model.organization.invitation.InvitationRepositoryProvider
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Unit tests for [InvitationRepositoryProvider].
 *
 * Verifies that the repository instance can be replaced and retrieved correctly.
 */
class InvitationRepositoryProviderTest {
  @Test
  fun repository_returns_the_instance_we_set() {
    // Create a fake implementation for testing
    val fake =
        object : InvitationRepository {
          override suspend fun getAllInvitations(): List<Invitation> = emptyList()

          override suspend fun insertInvitation(organization: Organization, user: User) {}

          override suspend fun updateInvitation(
              itemId: String,
              item: Invitation,
              organization: Organization,
              user: User
          ) {}

          override suspend fun deleteInvitation(
              itemId: String,
              organization: Organization,
              user: User
          ) {}

          override suspend fun getInvitationById(itemId: String): Invitation? = null

          override suspend fun getInvitationByOrganization(
              organizationId: String
          ): List<Invitation> = emptyList()
        }

    // Replace the default repository with the fake
    InvitationRepositoryProvider.repository = fake

    // Ensure the provider now returns the same instance
    assertSame(fake, InvitationRepositoryProvider.repository)
  }
}
