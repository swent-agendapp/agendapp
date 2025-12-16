package com.android.sample.utils

import com.android.sample.model.network.FakeConnectivityChecker
import com.android.sample.model.network.NetworkStatusRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before

/**
 * An interface to be implemented by tests that require network connectivity manipulation.
 *
 * This interface provides methods to simulate changes in network connectivity before and during
 * tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
interface NetworkTestBase {

  /** The initial internet connectivity state for the test. Default is connected (true). */
  val initialInternetState: Boolean
    get() = true

  val fakeChecker: FakeConnectivityChecker

  /** The network status repository used in the tests. */
  val networkRepo: NetworkStatusRepository

  /** Default internet state constant (true). */
  companion object {
    const val DEFAULT_INTERNET_STATE = true
  }

  /**
   * Sets up the network test base before each test.
   *
   * Call this method in a @Before annotated function (e.g., in the setUp() method) of the test
   * class.
   */
  @Before
  fun setupNetworkTestBase() {
    fakeChecker.setInternet(initialInternetState)
  }

  /** Simulates loss of internet connectivity. */
  fun simulateNoInternet() {
    fakeChecker.setInternet(false)
  }

  /** Simulates restoration of internet connectivity. */
  fun simulateInternetRestored() {
    fakeChecker.setInternet(true)
  }
}
