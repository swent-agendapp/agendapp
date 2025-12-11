package com.android.sample.model.organizationRepositoryTest

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.repository.OrganizationRepositoryLocal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OrganizationRepositoryLocalTest {

  private lateinit var organizationRepository: OrganizationRepositoryLocal
  private lateinit var userRepository: UsersRepositoryLocal

  private lateinit var adminA: User
  private lateinit var adminB: User
  private lateinit var memberA: User
  private lateinit var memberB: User
  private lateinit var outsider: User

  private lateinit var orgA: Organization
  private lateinit var orgB: Organization
  private lateinit var orgC: Organization

  @Before
  fun setup() = runBlocking {
    // Initialize fresh UserRepository for each test
    userRepository = UsersRepositoryLocal()
    organizationRepository = OrganizationRepositoryLocal(userRepository = userRepository)

    // --- Create users ---
    adminA = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
    adminB = User(id = "adminB", displayName = "Admin B", email = "adminB@example.com")
    memberA = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")
    memberB = User(id = "memberB", displayName = "Member B", email = "memberB@example.com")
    outsider = User(id = "outsider", displayName = "Outsider", email = "outsider@example.com")

    // Register all users in the repository
    userRepository.newUser(adminA)
    userRepository.newUser(adminB)
    userRepository.newUser(memberA)
    userRepository.newUser(memberB)
    userRepository.newUser(outsider)

    // --- Create organizations ---
    orgA = Organization(id = "orgA", name = "Org A")
    orgB = Organization(id = "orgB", name = "Org B")
    orgC = Organization(id = "orgC", name = "Org C")
  }

  // Insertion tests
  @Test
  fun insertOrganization_asAdmin_shouldSucceed() = runBlocking {
    // Insert organization
    organizationRepository.insertOrganization(orgA)

    // Add adminA as admin of orgA
    userRepository.addAdminToOrganization(adminA.id, orgA.id)

    // Refresh adminA to have updated organizations list
    val updatedAdminA = userRepository.getUsersByIds(listOf(adminA.id)).first()
    val organizations = organizationRepository.getAllOrganizations(updatedAdminA)
    assertEquals(1, organizations.size)
    assertEquals("Org A", organizations.first().name)

    // Insert another organization
    organizationRepository.insertOrganization(orgC)
    userRepository.addAdminToOrganization(adminA.id, orgC.id)

    val finalAdminA = userRepository.getUsersByIds(listOf(adminA.id)).first()
    val updatedOrganizations = organizationRepository.getAllOrganizations(finalAdminA)
    assertEquals(2, updatedOrganizations.size)
    assertEquals("Org C", updatedOrganizations.find { it.id == "orgC" }?.name)
  }

  // Note: insertOrganization doesn't have a user parameter in the interface,
  // so we cannot test admin-only insertion. The test below is removed.
  // If you want to enforce admin-only insertion, the interface needs to be changed.

  @Test(expected = IllegalArgumentException::class)
  fun insertOrganization_withDuplicateId_shouldThrow() = runBlocking {
    organizationRepository.insertOrganization(orgA)
    organizationRepository.insertOrganization(orgA.copy(name = "Duplicate"))
  }

  // Fetching & visibility tests
  @Test
  fun getAllOrganizations_shouldReturnOnlyAccessibleOnes() = runBlocking {
    organizationRepository.insertOrganization(orgA)
    organizationRepository.insertOrganization(orgB)
    organizationRepository.insertOrganization(orgC)

    // Set up user-organization relationships
    userRepository.addAdminToOrganization(adminA.id, orgA.id)
    userRepository.addAdminToOrganization(adminA.id, orgC.id) // adminA has access to orgC
    userRepository.addAdminToOrganization(adminB.id, orgB.id)
    userRepository.addAdminToOrganization(adminB.id, orgC.id) // adminB also has access to orgC
    userRepository.addUserToOrganization(memberA.id, orgA.id)
    userRepository.addUserToOrganization(memberB.id, orgB.id)
    userRepository.addUserToOrganization(memberB.id, orgC.id) // memberB has access to orgC

    // Get updated user objects with organizations list
    val updatedAdminA = userRepository.getUsersByIds(listOf(adminA.id)).first()
    val updatedMemberB = userRepository.getUsersByIds(listOf(memberB.id)).first()
    val updatedOutsider = userRepository.getUsersByIds(listOf(outsider.id)).first()

    val adminAOrganizations = organizationRepository.getAllOrganizations(updatedAdminA)
    val memberBOrganizations = organizationRepository.getAllOrganizations(updatedMemberB)
    val outsiderOrganizations = organizationRepository.getAllOrganizations(updatedOutsider)

    assertEquals(setOf("orgA", "orgC"), adminAOrganizations.map { it.id }.toSet())
    assertEquals(setOf("orgB", "orgC"), memberBOrganizations.map { it.id }.toSet())
    assertTrue(outsiderOrganizations.isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun getOrganizationById_shouldRespectAccessRights() {
    runBlocking {
      organizationRepository.insertOrganization(orgA)
      organizationRepository.insertOrganization(orgB)

      // Set up permissions
      userRepository.addAdminToOrganization(adminA.id, orgA.id)
      userRepository.addUserToOrganization(memberB.id, orgB.id)

      val updatedAdminA = userRepository.getUsersByIds(listOf(adminA.id)).first()
      val updatedMemberB = userRepository.getUsersByIds(listOf(memberB.id)).first()

      // Admin can fetch their org
      val fetchedAdmin = organizationRepository.getOrganizationById("orgA", updatedAdminA)
      assertNotNull(fetchedAdmin)

      // Member can fetch their org
      val fetchedMember = organizationRepository.getOrganizationById("orgB", updatedMemberB)
      assertNotNull(fetchedMember)

      // Outsider should trigger an IllegalArgumentException
      organizationRepository.getOrganizationById("orgA", outsider)
    }
  }

  // Update tests
  @Test
  fun updateOrganization_asAdmin_shouldModifyIt() = runBlocking {
    organizationRepository.insertOrganization(orgA)

    // Make adminA an admin of orgA
    userRepository.addAdminToOrganization(adminA.id, orgA.id)
    val updatedAdminA = userRepository.getUsersByIds(listOf(adminA.id)).first()

    val updated = orgA.copy(name = "Org A Updated")
    organizationRepository.updateOrganization(orgA.id, updated, updatedAdminA)
    val fetched = organizationRepository.getOrganizationById(orgA.id, updatedAdminA)

    assertEquals("Org A Updated", fetched?.name)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateOrganization_asNonAdmin_shouldThrow() = runBlocking {
    organizationRepository.insertOrganization(orgA)

    // Make memberA a member (not admin) of orgA
    userRepository.addUserToOrganization(memberA.id, orgA.id)
    val updatedMemberA = userRepository.getUsersByIds(listOf(memberA.id)).first()

    val updated = orgA.copy(name = "Invalid Update")
    organizationRepository.updateOrganization(orgA.id, updated, updatedMemberA)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateOrganization_nonExisting_shouldThrow() = runBlocking {
    organizationRepository.updateOrganization("unknown", orgA, adminA)
  }

  // Deletion tests
  @Test
  fun deleteOrganization_asAdmin_shouldRemoveIt() = runBlocking {
    organizationRepository.insertOrganization(orgA)
    organizationRepository.insertOrganization(orgC)

    // Make adminA an admin of both organizations
    userRepository.addAdminToOrganization(adminA.id, orgA.id)
    userRepository.addAdminToOrganization(adminA.id, orgC.id)
    val updatedAdminA = userRepository.getUsersByIds(listOf(adminA.id)).first()

    organizationRepository.deleteOrganization(orgA.id, updatedAdminA)

    val all = organizationRepository.getAllOrganizations(updatedAdminA)
    assertEquals(listOf("orgC"), all.map { it.id })
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteOrganization_asNonAdmin_shouldThrow() = runBlocking {
    organizationRepository.insertOrganization(orgA)

    // Make memberA a member (not admin) of orgA
    userRepository.addUserToOrganization(memberA.id, orgA.id)
    val updatedMemberA = userRepository.getUsersByIds(listOf(memberA.id)).first()

    organizationRepository.deleteOrganization(orgA.id, updatedMemberA)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteOrganization_nonExisting_shouldThrow() = runBlocking {
    organizationRepository.deleteOrganization("unknown", adminA)
  }

  // Combined behavior tests
  @Test
  fun complexScenario_multipleAdminsAndMembers_shouldMaintainConsistency() = runBlocking {
    // Insert all
    organizationRepository.insertOrganization(orgA)
    organizationRepository.insertOrganization(orgB)
    organizationRepository.insertOrganization(orgC)

    // Set up permissions
    userRepository.addAdminToOrganization(adminA.id, orgA.id)
    userRepository.addAdminToOrganization(adminA.id, orgC.id)
    userRepository.addAdminToOrganization(adminB.id, orgB.id)
    userRepository.addAdminToOrganization(adminB.id, orgC.id)
    userRepository.addUserToOrganization(memberB.id, orgC.id)

    val updatedAdminA = userRepository.getUsersByIds(listOf(adminA.id)).first()
    var updatedAdminB = userRepository.getUsersByIds(listOf(adminB.id)).first()
    var updatedMemberB = userRepository.getUsersByIds(listOf(memberB.id)).first()

    // Admin A updates orgC
    val updatedC = orgC.copy(name = "Org C Updated", geoCheckEnabled = true)
    organizationRepository.updateOrganization(orgC.id, updatedC, updatedAdminA)

    // Member B can still see updated orgC
    updatedMemberB = userRepository.getUsersByIds(listOf(memberB.id)).first()
    val organizationsForMemberB = organizationRepository.getAllOrganizations(updatedMemberB)
    val fetchedC = organizationsForMemberB.find { it.id == "orgC" }
    assertNotNull(fetchedC)
    assertTrue(fetchedC!!.geoCheckEnabled)
    assertEquals("Org C Updated", fetchedC.name)

    // Admin B deletes orgB
    updatedAdminB = userRepository.getUsersByIds(listOf(adminB.id)).first()
    organizationRepository.deleteOrganization("orgB", updatedAdminB)

    updatedAdminB = userRepository.getUsersByIds(listOf(adminB.id)).first()
    val adminBRemaining = organizationRepository.getAllOrganizations(updatedAdminB)
    assertEquals(setOf("orgC"), adminBRemaining.map { it.id }.toSet())
  }

  @Test
  fun getMembersOfOrganization_asMember_shouldReturnMembers() = runBlocking {
    organizationRepository.insertOrganization(orgC)

    // Set up members for orgC
    userRepository.addUserToOrganization(memberA.id, orgC.id)
    userRepository.addUserToOrganization(memberB.id, orgC.id)

    val members = userRepository.getMembersIds(orgC.id)

    assertEquals(setOf("memberA", "memberB"), members.toSet())
  }

  @Test
  fun getMembersOfOrganization_asOutsider_shouldReturnEmpty() {
    runBlocking {
      organizationRepository.insertOrganization(orgC)

      // Don't add any members
      val members = userRepository.getMembersIds(orgC.id)

      // Should return empty list, not throw
      assertTrue(members.isEmpty())
    }
  }
}
