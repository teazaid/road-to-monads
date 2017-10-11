package io.monadic.repository

import io.registration.models.db.tables.UserTable
import slick.lifted.TableQuery

class UserRepository {
  private val users = TableQuery[UserTable]
}
