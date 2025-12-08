package com.android.sample.model.organizationRepositoryTest

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepositoryLocal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OrganizationRepositoryLocalTest {

  private lateinit var repository: OrganizationRepositoryLocal

  private lateinit var adminA: User
  private lateinit var adminB: User
  private lateinit var memberA: User
  private lateinit var memberB: User
  private lateinit var outsider: User

  private lateinit var orgA: Organization
  private lateinit var orgB: Organization
  private lateinit var orgC: Organization

  @Before
  fun setup() {
    repository = OrganizationRepositoryLocal()

    // --- Create users ---
    adminA = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
    adminB = User(id = "adminB", displayName = "Admin B", email = "adminB@example.com")
    memberA = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")
    memberB = User(id = "memberB", displayName = "Member B", email = "memberB@example.com")
    outsider = User(id = "outsider", displayName = "Outsider", email = "outsider@example.com")

    // --- Create organizations ---
    orgA =
        Organization(
            id = "orgA", name = "Org A")

    orgB =
        Organization(
            id = "orgB", name = "Org B")

    orgC =
        Organization(
            id = "orgC",
            name = "Org C")
  }

  // Insertion tests
  @Test
  fun insertOrganization_asAdmin_shouldSucceed() = runBlocking {
    repository.insertOrganization(orgA)
    val organizations = repository.getAllOrganizations(adminA)
    assertEquals(1, organizations.size)
    assertEquals("Org A", organizations.first().name)

    repository.insertOrganization(orgC)
    val updatedOrganizations = repository.getAllOrganizations(adminA)
    assertEquals(2, updatedOrganizations.size)
    assertEquals("Org C", updatedOrganizations.find { it.id == "orgC" }?.name)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertOrganization_asNonAdmin_shouldThrow() = runBlocking {
    repository.insertOrganization(orgA)
  }

  @Test(expected = IllegalArgumentException::class)
  fun insertOrganization_withDuplicateId_shouldThrow() = runBlocking {
    repository.insertOrganization(orgA)
    repository.insertOrganization(orgA.copy(name = "Duplicate"))
  }

  // Fetching & visibility tests
  @Test
  fun getAllOrganizations_shouldReturnOnlyAccessibleOnes() = runBlocking {
    repository.insertOrganization(orgA)
    repository.insertOrganization(orgB)
    repository.insertOrganization(orgC)

    val adminAOrganizations = repository.getAllOrganizations(adminA)
    val memberBOrganizations = repository.getAllOrganizations(memberB)
    val outsiderOrganizations = repository.getAllOrganizations(outsider)

    assertEquals(setOf("orgA", "orgC"), adminAOrganizations.map { it.id }.toSet())
    assertEquals(setOf("orgB", "orgC"), memberBOrganizations.map { it.id }.toSet())
    assertTrue(outsiderOrganizations.isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun getOrganizationById_shouldRespectAccessRights() {
    runBlocking {
      repository.insertOrganization(orgA)
      repository.insertOrganization(orgB)

      // Admin can fetch their org
      val fetchedAdmin = repository.getOrganizationById("orgA", adminA)
      assertNotNull(fetchedAdmin)

      // Member can fetch their org
      val fetchedMember = repository.getOrganizationById("orgB", memberB)
      assertNotNull(fetchedMember)

      // Outsider should trigger an IllegalArgumentException
      repository.getOrganizationById("orgA", outsider)
    }
  }

  // Update tests
  @Test
  fun updateOrganization_asAdmin_shouldModifyIt() = runBlocking {
    repository.insertOrganization(orgA)
    val updated = orgA.copy(name = "Org A Updated")

    repository.updateOrganization(orgA.id, updated, adminA)
    val fetched = repository.getOrganizationById(orgA.id, adminA)

    assertEquals("Org A Updated", fetched?.name)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateOrganization_asNonAdmin_shouldThrow() = runBlocking {
    repository.insertOrganization(orgA)
    val updated = orgA.copy(name = "Invalid Update")
    repository.updateOrganization(orgA.id, updated, memberA)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateOrganization_nonExisting_shouldThrow() = runBlocking {
    repository.updateOrganization("unknown", orgA, adminA)
  }

  // Deletion tests
  @Test
  fun deleteOrganization_asAdmin_shouldRemoveIt() = runBlocking {
    repository.insertOrganization(orgA)
    repository.insertOrganization(orgC)

    repository.deleteOrganization(orgA.id, adminA)

    val all = repository.getAllOrganizations(adminA)
    assertEquals(listOf("orgC"), all.map { it.id })
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteOrganization_asNonAdmin_shouldThrow() = runBlocking {
    repository.insertOrganization(orgA)
    repository.deleteOrganization(orgA.id, memberA)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteOrganization_nonExisting_shouldThrow() = runBlocking {
    repository.deleteOrganization("unknown", adminA)
  }

  // Combined behavior tests
  @Test
  fun complexScenario_multipleAdminsAndMembers_shouldMaintainConsistency() = runBlocking {
    // Insert all
    repository.insertOrganization(orgA)
    repository.insertOrganization(orgB)
    repository.insertOrganization(orgC)

    // Admin A updates orgC
    val updatedC = orgC.copy(name = "Org C Updated", geoCheckEnabled = true)
    repository.updateOrganization(orgC.id, updatedC, adminA)

    // Member B can still see updated orgC
    val organizationsForMemberB = repository.getAllOrganizations(memberB)
    val fetchedC = organizationsForMemberB.find { it.id == "orgC" }
    assertNotNull(fetchedC)
    assertTrue(fetchedC!!.geoCheckEnabled)
    assertEquals("Org C Updated", fetchedC.name)

    // Admin B deletes orgB
    repository.deleteOrganization("orgB", adminB)

    val adminBRemaining = repository.getAllOrganizations(adminB)
    assertEquals(setOf("orgC"), adminBRemaining.map { it.id }.toSet())
  }

  @Test
  fun getMembersOfOrganization_asMember_shouldReturnMembers() = runBlocking {
    repository.insertOrganization(orgC)

    val members = UsersRepositoryLocal().getMembersIds(orgC.id)

    assertEquals(setOf("memberA", "memberB"), members)
  }

  @Test(expected = IllegalArgumentException::class)
  fun getMembersOfOrganization_asOutsider_shouldThrow() {
    runBlocking {
      repository.insertOrganization(orgC)
      UsersRepositoryLocal().getMembersIds(orgC.id)
    }
  }
}
