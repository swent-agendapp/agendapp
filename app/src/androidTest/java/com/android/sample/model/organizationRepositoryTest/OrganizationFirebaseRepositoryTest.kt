package com.android.sample.model.organizationRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.User
import com.android.sample.model.organizations.Organization
import com.android.sample.model.organizations.OrganizationRepository
import com.android.sample.utils.FirebaseEmulatedTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationFirebaseRepositoryTest : FirebaseEmulatedTest() {

  private lateinit var repository: OrganizationRepository

  private lateinit var adminA: User
  private lateinit var adminB: User
  private lateinit var memberA: User
  private lateinit var memberB: User
  private lateinit var outsider: User

  private lateinit var orgA: Organization
  private lateinit var orgB: Organization
  private lateinit var orgC: Organization

  @Before
  override fun setUp() {
    super.setUp()
    repository = createInitializedOrganizationRepository()

    // --- Create users ---
    adminA = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
    adminB = User(id = "adminB", displayName = "Admin B", email = "adminB@example.com")
    memberA = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")
    memberB = User(id = "memberB", displayName = "Member B", email = "memberB@example.com")
    outsider = User(id = "outsider", displayName = "Outsider", email = "outsider@example.com")

    // --- Create organizations ---
    orgA =
        Organization(
            id = "orgA", name = "Org A", admins = listOf(adminA), members = listOf(memberA, adminA))

    orgB =
        Organization(
            id = "orgB", name = "Org B", admins = listOf(adminB), members = listOf(memberB, adminB))

    orgC =
        Organization(
            id = "orgC",
            name = "Org C",
            admins = listOf(adminA, adminB),
            members = listOf(memberA, memberB))
  }

  // --- Insertion tests ---
  @Test
  fun insertOrganization_asAdmin_shouldSucceed() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    val fetched = repository.getOrganizationById("orgA", adminA)
    assertNotNull(fetched)
    assertEquals("Org A", fetched!!.name)
  }

  @Test
  fun insertOrganization_asNonAdmin_shouldThrow() = runBlocking {
    try {
      repository.insertOrganization(orgA, memberA)
      fail("Expected IllegalArgumentException")
    } catch (_: IllegalArgumentException) {}
  }

  // --- Fetching & visibility ---
  @Test
  fun getAllOrganizations_shouldReturnAllInsertedAndAccessible() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    repository.insertOrganization(orgB, adminB)
    repository.insertOrganization(orgC, adminA)

    val all =
        (repository.getAllOrganizations(adminA) + repository.getAllOrganizations(memberB)).toSet()
    assertEquals(3, all.size)
  }

  @Test
  fun getOrganizationById_shouldWorkForAdminsAndMembers() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    repository.insertOrganization(orgB, adminB)

    val adminResult = repository.getOrganizationById("orgA", adminA)
    val memberResult = repository.getOrganizationById("orgB", memberB)

    assertNotNull(adminResult)
    assertNotNull(memberResult)
    assertEquals("Org A", adminResult!!.name)
    assertEquals("Org B", memberResult!!.name)
  }

  @Test
  fun getOrganizationById_asOutsider_shouldThrow() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    try {
      repository.getOrganizationById("orgA", outsider)
      fail("Expected IllegalArgumentException for outsider")
    } catch (_: IllegalArgumentException) {}
  }

  // --- Update tests ---
  @Test
  fun updateOrganization_asAdmin_shouldModifyIt() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    val updated = orgA.copy(name = "Org A Updated", geoCheckEnabled = true)
    repository.updateOrganization(orgA.id, updated, adminA)

    val fetched = repository.getOrganizationById("orgA", adminA)
    assertEquals("Org A Updated", fetched?.name)
    assertTrue(fetched!!.geoCheckEnabled)
  }

  @Test
  fun updateOrganization_asNonAdmin_shouldThrow() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    val updated = orgA.copy(name = "Illegal Update")
    try {
      repository.updateOrganization(orgA.id, updated, memberA)
      fail("Expected IllegalArgumentException")
    } catch (_: IllegalArgumentException) {}
  }

  // --- Deletion tests ---
  @Test
  fun deleteOrganization_asAdmin_shouldRemoveIt() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    repository.insertOrganization(orgB, adminB)

    repository.deleteOrganization("orgA", adminA)
    val fetched = repository.getOrganizationById("orgA", adminA)
    assertNull(fetched)
  }

  @Test
  fun deleteOrganization_asNonAdmin_shouldThrow() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    try {
      repository.deleteOrganization("orgA", memberA)
      fail("Expected IllegalArgumentException")
    } catch (_: IllegalArgumentException) {}
  }

  // --- Complex scenario ---
  @Test
  fun complexScenario_multipleAdminsAndMembers_shouldStayConsistent() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    repository.insertOrganization(orgB, adminB)
    repository.insertOrganization(orgC, adminA)

    val updatedC = orgC.copy(name = "Org C Updated", geoCheckEnabled = true)
    repository.updateOrganization("orgC", updatedC, adminB)

    val organizationsForMemberA = repository.getAllOrganizations(memberA)
    val fetchedC = organizationsForMemberA.find { it.id == "orgC" }
    assertNotNull(fetchedC)
    assertEquals("Org C Updated", fetchedC!!.name)
    assertTrue(fetchedC.geoCheckEnabled)

    repository.deleteOrganization("orgB", adminB)
    val adminBRemaining = repository.getAllOrganizations(adminB)
    assertEquals(setOf("orgC"), adminBRemaining.map { it.id }.toSet())
  }

  @Test
  fun getMembersOfOrganization_asMember_shouldReturnMembers() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    val members = repository.getMembersOfOrganization(orgA.id, memberA)
    val memberIds = members.map { it.id }.toSet()
    assertEquals(setOf("memberA", "adminA"), memberIds)
  }

  @Test
  fun getMembersOfOrganization_asOutsider_shouldThrow() = runBlocking {
    repository.insertOrganization(orgA, adminA)
    try {
      repository.getMembersOfOrganization(orgA.id, outsider)
      fail("Expected IllegalArgumentException for outsider")
    } catch (_: IllegalArgumentException) {}
  }
}
