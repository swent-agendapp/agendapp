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
  override val fakeChecker = FakeConnectivityChecker(state = true)
  override val networkRepo = NetworkStatusRepository(fakeChecker)

  @Before
  fun setup() {
    setupNetworkTestBase()
  }

  @Test
  fun testInitialConnectivityState() = runBlocking {
    val isConnected = networkRepo.isConnected.value
    assertTrue(isConnected)
  }

  @Test
  fun testInitialNoConnectivityState() = runBlocking {
    simulateNoInternet()

    val isConnected = networkRepo.isConnected.value
    Assert.assertFalse(isConnected)
  }

  @Test
  fun testConnectivityChange() = runBlocking {
    // Initial state should be connected
    assertTrue(networkRepo.isConnected.value)

    // Simulate loss of connectivity
    simulateNoInternet()
    Assert.assertEquals(false, networkRepo.isConnected.value)

    // Simulate restoration of connectivity
    simulateInternetRestored()
    Assert.assertEquals(true, networkRepo.isConnected.value)
  }
}
