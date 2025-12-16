package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.data.local.BoxProvider
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.components.AddEventAttendantBottomBar
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventAttendantBottomBarTest : RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    setSelectedOrganization()
    val context =
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
    BoxProvider.init(context)
    EventRepositoryProvider.repository = EventRepositoryLocal()
    val userLocal = UsersRepositoryLocal()
    UserRepositoryProvider.repository = userLocal
    EventRepositoryProvider.repository = EventRepositoryLocal()

    composeTestRule.setContent { AddEventAttendantBottomBar() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsDisplayed()
  }
}
