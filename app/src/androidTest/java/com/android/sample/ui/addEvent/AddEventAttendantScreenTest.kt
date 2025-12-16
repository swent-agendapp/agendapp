package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.data.local.BoxProvider
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.components.AddEventAttendantScreen
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventAttendantScreenTest : RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    setSelectedOrganization()
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    BoxProvider.init(context)

    UserRepositoryProvider.repository = UsersRepositoryLocal()
    EventRepositoryProvider.repository = EventRepositoryLocal()
    composeTestRule.setContent { AddEventAttendantScreen() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.LIST_USER).assertIsDisplayed()
  }
}
