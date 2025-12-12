package com.android.sample.model.category.mockData

import com.android.sample.model.category.EventCategory
import com.android.sample.ui.theme.EventPalette

/**
 * Provides mock event categories for previews and tests.
 *
 * The list contains several predefined categories matching the different age groups and activity
 * types of our stakeholder.
 */
fun getMockEventCategory(orgId: String = "mock_orgId"): List<EventCategory> {
  return listOf(
      EventCategory(
          organizationId = orgId,
          label = "Parent-Enfant",
          color = EventPalette.BrownOrange,
      ),
      EventCategory(
          organizationId = orgId,
          label = "3-4 ans",
          color = EventPalette.Blue,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Loisirs 1-2P",
          color = EventPalette.LightBlue,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Loisirs 3-5P",
          color = EventPalette.Indigo,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Loisirs 6-8P",
          color = EventPalette.PinkViolet,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Loisirs 9-11P",
          color = EventPalette.Orange,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Junior 1",
          color = EventPalette.Green,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Junior 2",
          color = EventPalette.Green,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Adultes",
          color = EventPalette.Purple,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Formation",
          color = EventPalette.BeigeSalmon,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Sport-étude",
          color = EventPalette.BeigeSalmon,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Spécialité",
          color = EventPalette.Pink,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Compétitions",
          color = EventPalette.Red,
      ),
      EventCategory(
          organizationId = orgId,
          label = "Divers",
          color = EventPalette.Yellow,
      ),
  )
}
