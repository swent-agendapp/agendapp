package com.android.sample.ui.calendar

import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.data.local.repositories.EventRepositoryInMemory
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryInMemory
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepository
import com.android.sample.model.category.EventCategoryRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import com.android.sample.ui.theme.EventPalette
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEventViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var eventRepository: EventRepository
  private lateinit var categoryRepository: EventCategoryRepository
  private lateinit var userRepository: UserRepository
  private lateinit var authRepository: AuthRepository
  private lateinit var organizationRepository: OrganizationRepository

  private lateinit var user: User
  private lateinit var organization: Organization

  private val selectedOrganizationID: String = "org123"

  @Before
  fun setUp() = runBlocking {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryInMemory()
    userRepository = UsersRepositoryLocal()
    authRepository = FakeAuthRepository()
    organizationRepository = OrganizationRepositoryLocal(userRepository)

    user =
        User(
            id = "adminA",
            displayName = "Admin A",
            email = "adminA@example.com",
            organizations = listOf(selectedOrganizationID))

    // Register user in repo
    userRepository.newUser(user)

    // Create organization
    organization = Organization(id = selectedOrganizationID, name = "Org A")
    organizationRepository.insertOrganization(organization)

    // Link user <-> organization
    userRepository.addAdminToOrganization(user.id, organization.id)

    // Set selected organization
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)

    // Use a fake category repository to control loadCategories() deterministically
    categoryRepository = EventCategoryRepositoryLocal()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ------------------------------------------------------------
  //  UI State tests
  // ------------------------------------------------------------

  @Test
  fun `initial UI state has default values`() {
    val vm = makeVm()
    val state = vm.uiState.value

    assertTrue(state.title.isEmpty())
    assertTrue(state.description.isEmpty())
    assertEquals(0, state.participants.size)
    assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
    assertEquals(EventCategory.defaultCategory().label, state.category.label)
    assertEquals(EventCategory.defaultCategory().color, state.category.color)
    assertEquals(EventCategory.defaultCategory().isDefault, state.category.isDefault)
    assertFalse(state.isLoadingCategories)
    assertNull(state.errorMsg)
  }

  @Test
  fun `setTitle updates the title in UI state`() {
    val vm = makeVm()
    vm.setTitle("SwEnt Meeting")
    assertEquals("SwEnt Meeting", vm.uiState.value.title)
  }

  @Test
  fun `setCategory updates the category in UI state`() {
    val vm = makeVm()
    val newCategory =
        EventCategory(
            id = "cat-1",
            label = "Cat 1",
            color = EventPalette.Blue,
            isDefault = false,
            index = 1,
            organizationId = selectedOrganizationID)

    vm.setCategory(newCategory)

    assertEquals(newCategory, vm.uiState.value.category)
  }

  @Test
  fun `setDescription updates the description in UI state`() {
    val vm = makeVm()
    vm.setDescription("Standup meeting")
    assertEquals("Standup meeting", vm.uiState.value.description)
  }

  @Test
  fun `setStartInstant updates the start instant in UI state`() {
    val vm = makeVm()
    val newStart = Instant.parse("2025-03-01T10:00:00Z")
    vm.setStartInstant(newStart)
    assertEquals(newStart, vm.uiState.value.startInstant)
  }

  @Test
  fun `setEndInstant updates the end instant in UI state`() {
    val vm = makeVm()
    val newEnd = Instant.parse("2025-03-01T11:00:00Z")
    vm.setEndInstant(newEnd)
    assertEquals(newEnd, vm.uiState.value.endInstant)
  }

  @Test
  fun `addParticipant and removeParticipant modify participants set`() {
    val vm = makeVm()
    val user = User(id = "user1")
    vm.addParticipant(user)
    assertTrue(vm.uiState.value.participants.contains(user))

    vm.removeParticipant(user)
    assertFalse(vm.uiState.value.participants.contains(user))
  }

  @Test
  fun `allFieldsValid returns false if title or description is blank`() {
    val vm = makeVm()

    vm.setTitle("")
    vm.setDescription("desc")
    assertFalse(vm.allFieldsValid())

    vm.setTitle("Title")
    vm.setDescription("")
    assertFalse(vm.allFieldsValid())

    vm.setTitle("Title")
    vm.setDescription("Desc")
    assertTrue(vm.allFieldsValid())
  }

  @Test
  fun `startTimeIsAfterEndTime returns true if start instant is after end instant`() {
    val vm = makeVm()
    val now = Instant.now()

    vm.setStartInstant(now.plus(Duration.ofHours(2)))
    vm.setEndInstant(now.plus(Duration.ofHours(1)))
    assertTrue(vm.startTimeIsAfterEndTime())

    vm.setStartInstant(now)
    vm.setEndInstant(now.plus(Duration.ofHours(1)))
    assertFalse(vm.startTimeIsAfterEndTime())
  }

  @Test
  fun `resetUiState clears all fields to default`() {
    val vm = makeVm()
    val user = User(id = "user1")

    vm.setTitle("Some Title")
    vm.setDescription("Some Description")
    vm.addParticipant(user)
    vm.setStartInstant(Instant.parse("2025-03-01T10:00:00Z"))
    vm.setEndInstant(Instant.parse("2025-03-01T11:00:00Z"))
    vm.setRecurrenceMode(RecurrenceStatus.Weekly)

    vm.resetUiState()
    val state = vm.uiState.value

    assertTrue(state.title.isEmpty())
    assertTrue(state.description.isEmpty())
    assertEquals(0, state.participants.size)
    assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
    assertEquals(EventCategory.defaultCategory().label, state.category.label)
    assertEquals(EventCategory.defaultCategory().color, state.category.color)
    assertEquals(EventCategory.defaultCategory().isDefault, state.category.isDefault)
  }

  // ------------------------------------------------------------
  //  Event creation tests
  // ------------------------------------------------------------

  @Test
  fun `addEvent inserts event into repository`() = runTest {
    val vm = makeVm()

    vm.setTitle("Meeting")
    vm.setDescription("Team sync")
    vm.setStartInstant(Instant.now())
    vm.setEndInstant(Instant.now().plus(Duration.ofHours(1)))

    vm.addEvent()
    testDispatcher.scheduler.advanceUntilIdle()

    val events = eventRepository.getAllEvents(selectedOrganizationID)
    assertTrue(events.any { it.title == "Meeting" && it.description == "Team sync" })
  }

  @Test
  fun `setIsExtra flags event as extra`() = runTest {
    val vm = makeVm()

    vm.setTitle("Extra shift")
    vm.setDescription("Evening support")
    vm.setIsExtra(true)

    vm.addEvent()
    testDispatcher.scheduler.advanceUntilIdle()

    val events = eventRepository.getAllEvents(selectedOrganizationID)
    assertTrue(events.any { it.isExtra })
  }

  // ------------------------------------------------------------
  //  Categories loading tests (AI used)
  // ------------------------------------------------------------

  @Test
  fun `loadCategories updates categoriesList sorted by index and clears loading flag`() = runTest {
    // Given
    val categories =
        listOf(
            EventCategory(
                id = "c2",
                label = "Cat 2",
                color = EventPalette.Blue,
                isDefault = false,
                index = 2,
                organizationId = selectedOrganizationID),
            EventCategory(
                id = "c0",
                label = "Cat 0",
                color = EventPalette.Purple,
                isDefault = false,
                index = 0,
                organizationId = selectedOrganizationID),
            EventCategory(
                id = "c1",
                label = "Cat 1",
                color = EventPalette.LightGreen,
                isDefault = false,
                index = 1,
                organizationId = selectedOrganizationID),
        )
    val repo = categoryRepository as EventCategoryRepositoryLocal
    categories.forEach { repo.insertCategory(orgId = selectedOrganizationID, it) }

    val vm = makeVm()
    testDispatcher.scheduler.advanceUntilIdle() // let init finish first

    // When
    vm.loadCategories()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = vm.uiState.value
    assertFalse(state.isLoadingCategories)
    assertNull(state.errorMsg)
    assertEquals(listOf("c0", "c1", "c2"), state.categoriesList.map { it.id })
  }

  /* Helper functions */

  private fun makeVm(): AddEventViewModel {
    return AddEventViewModel(
        userRepository = userRepository,
        authRepository = authRepository,
        eventRepository = eventRepository,
        categoryRepository = categoryRepository,
    )
  }
}
