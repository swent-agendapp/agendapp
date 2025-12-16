package com.android.sample.model.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.utils.NetworkTestBase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkStatusRepositoryTest : NetworkTestBase {
  override lateinit var fakeChecker: FakeConnectivityChecker
  override lateinit var networkRepo: NetworkStatusRepository

  @Before
  fun setup() {
    // Initialize with fake checker to control connectivity state
    fakeChecker = FakeConnectivityChecker(state = true) // Default state is connected
    networkRepo = NetworkStatusRepository(fakeChecker)
  }

  @Test
  fun testInitialConnectivityState() = runBlocking {
    val isConnected = networkRepo.isConnected.value
    assertTrue(isConnected)
  }

  @Test
  fun testInitialNoConnectivityState() = runBlocking {
    fakeChecker.setInternet(false)

    val isConnected = networkRepo.isConnected.value
    Assert.assertFalse(isConnected)
  }

  @Test
  fun testConnectivityChange() = runBlocking {
    // Initial state should be connected
    assertTrue(networkRepo.isConnected.value)

    // Simulate loss of connectivity
    fakeChecker.setInternet(false)
    Assert.assertEquals(false, networkRepo.isConnected.value)

    // Simulate restoration of connectivity
    fakeChecker.setInternet(true)
    Assert.assertEquals(true, networkRepo.isConnected.value)
  }
}
