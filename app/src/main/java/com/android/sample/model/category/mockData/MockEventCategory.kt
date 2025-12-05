package com.android.sample.model.category.mockData

import com.android.sample.model.category.EventCategory
import com.android.sample.ui.theme.EventPalette

/**
 * Provides mock event categories for previews and tests.
 *
 * The list contains several predefined categories matching the different age groups and activity
 * types of our stakeholder.
 */
fun getMockEventCategory(): List<EventCategory> {
  return listOf(
      EventCategory(
          label = "Parent-Enfant",
          color = EventPalette.BrownOrange,
      ),
      EventCategory(
          label = "3-4 ans",
          color = EventPalette.Blue,
      ),
      EventCategory(
          label = "Loisirs 1-2P",
          color = EventPalette.LightBlue,
      ),
      EventCategory(
          label = "Loisirs 3-5P",
          color = EventPalette.Indigo,
      ),
      EventCategory(
          label = "Loisirs 6-8P",
          color = EventPalette.PinkViolet,
      ),
      EventCategory(
          label = "Loisirs 9-11P",
          color = EventPalette.Orange,
      ),
      EventCategory(
          label = "Junior 1",
          color = EventPalette.Green,
      ),
      EventCategory(
          label = "Junior 2",
          color = EventPalette.Green,
      ),
      EventCategory(
          label = "Adultes",
          color = EventPalette.Purple,
      ),
      EventCategory(
          label = "Formation",
          color = EventPalette.BeigeSalmon,
      ),
      EventCategory(
          label = "Sport-étude",
          color = EventPalette.BeigeSalmon,
      ),
      EventCategory(
          label = "Spécialité",
          color = EventPalette.Pink,
      ),
      EventCategory(
          label = "Compétitions",
          color = EventPalette.Red,
      ),
      EventCategory(
          label = "Divers",
          color = EventPalette.Yellow,
      ),
  )
}
