package com.android.sample.data.local

interface LocalStorable {
  val id: String
  val organizationId: String
  val version: Long
  val hasBeenDeleted: Boolean
}
