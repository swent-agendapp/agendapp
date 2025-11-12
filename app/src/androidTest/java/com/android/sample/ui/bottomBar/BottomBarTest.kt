package com.android.sample.ui.bottomBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.common.BottomBar
import com.android.sample.ui.common.BottomBarItem
import com.android.sample.ui.common.BottomBarTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var clicked: MutableMap<String, Boolean>
  private lateinit var items: List<BottomBarItem>

  @Before
  fun setUp() {
    clicked = mutableMapOf("home" to false, "settings" to false)

    items =
        listOf(
            BottomBarItem(
                icon = Icons.Default.Home,
                label = "Home",
                route = "home",
                onClick = { clicked["home"] = true },
                testTag = BottomBarTestTags.ITEM_CALENDAR,
                isSelected = true),
            BottomBarItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                route = "settings",
                onClick = { clicked["settings"] = true },
                testTag = BottomBarTestTags.ITEM_SETTINGS,
                isSelected = false))
  }

  @Test
  fun bottomBar_displaysAllItems() {
    composeTestRule.setContent { BottomBar(items = items) }

    composeTestRule.onNodeWithTag(BottomBarTestTags.BOTTOM_BAR).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_CALENDAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).assertIsDisplayed()
  }

  @Test
  fun bottomBar_itemClick_updatesState() {
    composeTestRule.setContent { BottomBar(items = items) }

    // Clique sur Home
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_CALENDAR).performClick()
    assert(clicked["home"] == true)

    // Clique sur Settings
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).performClick()
    assert(clicked["settings"] == true)
  }
}
