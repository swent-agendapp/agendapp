package com.android.sample.model.versioning

import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.Organization
import com.android.sample.model.organization.OrganizationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VersionSyncViewModelTest {

  @get:Rule val dispatcherRule = MainDispatcherRule()

  private val admin = User(id = "admin", displayName = "Admin", email = "admin@example.com")

  @Test
  fun pull_updatesLocalRepository_whenRemoteVersionIsNewer() = runTest {
    val localRepo = FakeOrganizationRepository(mutableListOf(sampleOrganization(version = 100L)))
    val remoteOrg = sampleOrganization(name = "Remote Org", version = 200L)
    val remoteRepo = FakeOrganizationRepository(mutableListOf(remoteOrg))
    val dispatcher = StandardTestDispatcher(testScheduler)

    val viewModel = makeViewModel(localRepo, remoteRepo, dispatcher)

    viewModel.pull()
    advanceUntilIdle()

    val localCopy = localRepo.getOrganizationById(remoteOrg.id, admin)

    assertEquals(remoteOrg.version, localCopy?.version)
    assertEquals("Remote Org", localCopy?.name)
  }

  @Test
  fun pull_insertsRemoteOrganization_whenMissingLocally() = runTest {
    val localRepo = FakeOrganizationRepository()
    val remoteOrg = sampleOrganization(name = "Only Remote", version = 42L)
    val remoteRepo = FakeOrganizationRepository(mutableListOf(remoteOrg))
    val dispatcher = StandardTestDispatcher(testScheduler)

    val viewModel = makeViewModel(localRepo, remoteRepo, dispatcher)

    viewModel.pull()
    advanceUntilIdle()

    val localCopy = localRepo.getOrganizationById(remoteOrg.id, admin)

    assertEquals(remoteOrg.version, localCopy?.version)
    assertEquals("Only Remote", localCopy?.name)
  }

  @Test
  fun push_updatesVersionAndPersistsAcrossRepositories() = runTest {
    val original = sampleOrganization(name = "Local Org", version = 1L)
    val localRepo = FakeOrganizationRepository(mutableListOf(original))
    val remoteRepo = FakeOrganizationRepository()
    val dispatcher = StandardTestDispatcher(testScheduler)

    val viewModel = makeViewModel(localRepo, remoteRepo, dispatcher)

    viewModel.push(original)
    advanceUntilIdle()

    val localCopy = localRepo.getOrganizationById(original.id, admin)
    val remoteCopy = remoteRepo.getOrganizationById(original.id, admin)

    require(localCopy != null && remoteCopy != null)

    assertTrue(localCopy.version > original.version)
    assertEquals(localCopy.version, remoteCopy.version)
  }

  private fun makeViewModel(
      local: FakeOrganizationRepository,
      remote: FakeOrganizationRepository,
      dispatcher: StandardTestDispatcher,
  ): VersionSyncViewModel {
    return VersionSyncViewModel(
        localOrganizationRepository = local,
        remoteOrganizationRepository = remote,
        authRepository = FakeAuthRepository(admin),
        dispatcher = dispatcher,
    )
  }

  private fun sampleOrganization(
      id: String = "org-1",
      name: String = "Org",
      version: Long = 123L,
  ): Organization {
    return Organization(
        id = id, name = name, admins = listOf(admin), members = listOf(admin), version = version)
  }
}

private class FakeOrganizationRepository(
    private val organizations: MutableMap<String, Organization> = mutableMapOf()
) : OrganizationRepository {

  constructor(initial: MutableList<Organization>) : this(initial.associateBy { it.id }.toMutableMap())

  override suspend fun getAllOrganizations(user: User): List<Organization> = organizations.values.toList()

  override suspend fun insertOrganization(organization: Organization, user: User) {
    organizations[organization.id] = organization
  }

  override suspend fun updateOrganization(
      organizationId: String,
      organization: Organization,
      user: User,
  ) {
    organizations[organizationId] = organization
  }

  override suspend fun deleteOrganization(organizationId: String, user: User) {
    organizations.remove(organizationId)
  }

  override suspend fun getOrganizationById(organizationId: String, user: User): Organization? {
    return organizations[organizationId]
  }

  override suspend fun getMembersOfOrganization(organizationId: String, user: User): List<User> {
    return organizations[organizationId]?.members ?: emptyList()
  }
}
