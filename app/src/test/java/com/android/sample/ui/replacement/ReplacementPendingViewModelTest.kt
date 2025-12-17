package com.android.sample.ui.replacement

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementRepository
import com.android.sample.model.replacement.ReplacementRepositoryLocal
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import java.lang.RuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// Assisted by IA
@OptIn(ExperimentalCoroutinesApi::class)
class ReplacementPendingViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var fakeRepository: FakeReplacementRepository

  private val selectedOrganizationID = "org123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    SelectedOrganizationVMProvider.viewModel.selectOrganization(selectedOrganizationID)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private class FakeReplacementRepository(
      private val replacementsToReturn: List<Replacement> = emptyList(),
      private val shouldThrow: Boolean = false,
      private val backing: ReplacementRepository = ReplacementRepositoryLocal()
  ) : ReplacementRepository by backing {

    var lastOrgId: String? = null
      private set

    override suspend fun getAllReplacements(organizationId: String): List<Replacement> {
      lastOrgId = organizationId
      if (shouldThrow) {
        throw RuntimeException("Test error")
      }
      return replacementsToReturn
    }
  }

  private class FakeUserRepository(private val usersToReturn: List<User> = emptyList()) :
      UserRepository {
    override suspend fun getMembersIds(organizationId: String): List<String> {
      return usersToReturn.map { it.id }
    }

    override suspend fun getUsersByIds(userIds: List<String>): List<User> {
      return usersToReturn.filter { it.id in userIds }
    }

    override suspend fun getAdminsIds(organizationId: String): List<String> = emptyList()

    override suspend fun newUser(user: User) {}

    override suspend fun deleteUser(userId: String) {}

    override suspend fun addUserToOrganization(userId: String, organizationId: String) {}

    override suspend fun addAdminToOrganization(userId: String, organizationId: String) {}
  }

  private fun makeVm(
      replacements: List<Replacement> = emptyList(),
      users: List<User> = emptyList(),
      shouldThrow: Boolean = false
  ): ReplacementPendingViewModel {
    fakeRepository =
        FakeReplacementRepository(replacementsToReturn = replacements, shouldThrow = shouldThrow)
    val fakeUserRepository = FakeUserRepository(usersToReturn = users)

    return ReplacementPendingViewModel(
        repository = fakeRepository,
        userRepository = fakeUserRepository,
        selectedOrganizationViewModel = SelectedOrganizationVMProvider.viewModel)
  }

  @Test
  fun `initial state has default values`() {
    val vm = makeVm()
    val state = vm.uiState.value

    assertFalse(state.isLoading)
    assertTrue(state.toProcess.isEmpty())
    assertTrue(state.waitingForAnswer.isEmpty())
    assertTrue(state.users.isEmpty())
    assertNull(state.errorMessage)
  }

  @Test
  fun `refresh with empty repository result keeps lists empty and clears error`() = runTest {
    val vm = makeVm(replacements = emptyList())

    vm.refresh()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertTrue(state.toProcess.isEmpty())
    assertTrue(state.waitingForAnswer.isEmpty())
    assertTrue(state.users.isEmpty())
    assertNull(state.errorMessage)

    assertEquals(selectedOrganizationID, fakeRepository.lastOrgId)
  }

  @Test
  fun `refresh sets errorMessage when repository throws`() = runTest {
    val vm = makeVm(replacements = emptyList(), shouldThrow = true)

    vm.refresh()

    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNotNull(state.errorMessage)
    assertTrue(state.errorMessage!!.contains("Failed to load pending replacements"))
  }

  @Test
  fun `refresh loads replacements and users`() = runTest {
    val user1 = User(id = "user1", displayName = "Alice", email = "alice@test.com")
    val user2 = User(id = "user2", displayName = "Bob", email = "bob@test.com")
    val users = listOf(user1, user2)

    val mockReplacements = com.android.sample.model.replacement.mockData.getMockReplacements()
    val event = mockReplacements.first().event
    val replacement1 =
        Replacement(
            id = "repl1",
            absentUserId = user1.id,
            substituteUserId = "",
            event = event,
            status = com.android.sample.model.replacement.ReplacementStatus.ToProcess)

    val vm = makeVm(replacements = listOf(replacement1), users = users)

    vm.refresh()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertEquals(1, state.toProcess.size)
    assertEquals(2, state.users.size)
    assertNull(state.errorMessage)
  }

  @Test
  fun `refresh filters out toProcess that have waiting replacements`() = runTest {
    val mockReplacements = com.android.sample.model.replacement.mockData.getMockReplacements()
    val event = mockReplacements.first().event
    val absentUserId = "user1"

    val toProcessReplacement =
        Replacement(
            id = "repl1",
            absentUserId = absentUserId,
            substituteUserId = "",
            event = event,
            status = com.android.sample.model.replacement.ReplacementStatus.ToProcess)

    val waitingReplacement =
        Replacement(
            id = "repl2",
            absentUserId = absentUserId,
            substituteUserId = "user2",
            event = event,
            status = com.android.sample.model.replacement.ReplacementStatus.WaitingForAnswer)

    val vm = makeVm(replacements = listOf(toProcessReplacement, waitingReplacement))

    vm.refresh()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    // toProcess should be filtered out since there's a waiting replacement for same event+user
    assertEquals(0, state.toProcess.size)
    assertEquals(1, state.waitingForAnswer.size)
  }

  @Test
  fun `refresh separates toProcess and waitingForAnswer correctly`() = runTest {
    val mockReplacements = com.android.sample.model.replacement.mockData.getMockReplacements()
    val event1 = mockReplacements[0].event
    val event2 = mockReplacements[1].event

    val toProcessReplacement =
        Replacement(
            id = "repl1",
            absentUserId = "user1",
            substituteUserId = "",
            event = event1,
            status = com.android.sample.model.replacement.ReplacementStatus.ToProcess)

    val waitingReplacement =
        Replacement(
            id = "repl2",
            absentUserId = "user2",
            substituteUserId = "user3",
            event = event2,
            status = com.android.sample.model.replacement.ReplacementStatus.WaitingForAnswer)

    val vm = makeVm(replacements = listOf(toProcessReplacement, waitingReplacement))

    vm.refresh()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(1, state.toProcess.size)
    assertEquals(1, state.waitingForAnswer.size)
    assertEquals("repl1", state.toProcess[0].id)
    assertEquals("repl2", state.waitingForAnswer[0].id)
  }
}
