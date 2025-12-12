package com.android.sample.model.organization.data

/** Provides mock organizations for ViewModel and repository testing. */
fun getMockOrganizations(): MutableList<Organization> {
  // Mock organizations
  val orgA = Organization(id = "O1", name = "Organization Alpha", geoCheckEnabled = false)

  val orgB = Organization(id = "O2", name = "Organization Beta", geoCheckEnabled = false)

  val orgC = Organization(id = "O3", name = "Organization Gamma", geoCheckEnabled = false)

  return mutableListOf(orgA, orgB, orgC)
}
