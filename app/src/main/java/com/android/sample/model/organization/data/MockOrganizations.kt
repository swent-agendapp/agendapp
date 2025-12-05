package com.android.sample.model.organization.data

import com.android.sample.model.authentication.User

/** Provides mock organizations for ViewModel and repository testing. */
fun getMockOrganizations(): MutableList<Organization> {

  // Mock users
  val adminA =
      User(
          id = "U1",
          displayName = "Alice Admin",
          email = "alice@example.com",
          phoneNumber = "123-456-7890")

  val adminB =
      User(
          id = "U2",
          displayName = "Bob Boss",
          email = "bob@example.com",
          phoneNumber = "234-567-8901")

  val memberC =
      User(
          id = "U3",
          displayName = "Charlie Member",
          email = "charlie@example.com",
          phoneNumber = "345-678-9012")

  val memberD =
      User(
          id = "U4",
          displayName = "Dana Member",
          email = "dana@example.com",
          phoneNumber = "456-789-0123")

  // Mock organizations
  val orgA =
      Organization(
          id = "O1",
          name = "Organization Alpha",
          admins = listOf(adminA.id),
          members = listOf(adminA.id, memberC.id),
          geoCheckEnabled = false)

  val orgB =
      Organization(
          id = "O2",
          name = "Organization Beta",
          admins = listOf(adminB.id),
          members = listOf(adminB.id, memberD.id),
          geoCheckEnabled = false)

  val orgC =
      Organization(
          id = "O3",
          name = "Organization Gamma",
          admins = listOf(adminA.id, adminB.id),
          members = listOf(adminA.id, adminB.id, memberC.id, memberD.id),
          geoCheckEnabled = false)

  return mutableListOf(orgA, orgB, orgC)
}
