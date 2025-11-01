package com.android.sample.model.organization

object EmployeeRepositoryProvider {
  private var _repository: EmployeeRepository? = null

  val repository: EmployeeRepository
    get() = _repository ?: error("EmployeeRepositoryProvider not initialized")

  fun init(repo: EmployeeRepository) {
    _repository = repo
  }
}
