package com.android.sample.model.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkStatusRepositoryTest {
  lateinit var fakeChecker: FakeConnectivityChecker
  lateinit var repo: NetworkStatusRepository

  @Before
  fun setup() {
    // Initialize with fake checker to control connectivity state
    fakeChecker = FakeConnectivityChecker(state = true) // Default state is connected
    repo = NetworkStatusRepository(fakeChecker)
  }

  @Test
  fun testInitialConnectivityState() = runBlocking {
    val isConnected = repo.isConnected.value
    assertTrue(isConnected)
  }

  @Test
  fun testInitialNoConnectivityState() = runBlocking {
    fakeChecker.setInternet(false)

    val isConnected = repo.isConnected.value
    Assert.assertFalse(isConnected)
  }

  @Test
  fun testConnectivityChange() = runBlocking {
    // Initial state should be connected
    assertTrue(repo.isConnected.value)

    // Simulate loss of connectivity
    fakeChecker.setInternet(false)
    Assert.assertEquals(false, repo.isConnected.value)

    // Simulate restoration of connectivity
    fakeChecker.setInternet(true)
    Assert.assertEquals(true, repo.isConnected.value)
  }
}
