package com.android.sample.ui.invitation

import com.android.sample.ui.invitation.createInvitation.CreateInvitationViewModel
import com.android.sample.ui.invitation.createInvitation.INVALID_INVITATION_COUNT_ERROR_MSG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateInvitationViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var vm: CreateInvitationViewModel

  @Before
  fun setUp() {
    vm = CreateInvitationViewModel()
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial UI state has count 1`() {
    val state = vm.uiState.value
    assertEquals(1, state.count)
  }

  @Test
  fun `setCount sets the count to specified value`() {
    vm.setCount(5)
    assertEquals(5, vm.uiState.value.count)

    vm.setCount(1)
    assertEquals(1, vm.uiState.value.count)
  }

  @Test
  fun `setCount with invalid values sets error message and does not change the count`() {
    vm.setCount(-1)
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
    assertEquals(1, vm.uiState.value.count)
    vm.setCount(150)
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
    assertEquals(1, vm.uiState.value.count)
  }

  @Test
  fun `increment increases count by 1 but never higher than 99`() {
    vm.increment()
    assertEquals(2, vm.uiState.value.count)

    vm.increment()
    assertEquals(3, vm.uiState.value.count)

    vm.setCount(99)
    assertEquals(99, vm.uiState.value.count)
    vm.increment()
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
  }

  @Test
  fun `decrement decreases count by 1 but never below 0`() {
    vm.decrement()
    assertEquals(1, vm.uiState.value.count)

    vm.setCount(3)
    vm.decrement()
    assertEquals(2, vm.uiState.value.count)
    vm.decrement()
    assertEquals(1, vm.uiState.value.count)

    vm.decrement()
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
  }
}
