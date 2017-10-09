package io.registration.repository

import io.registration.models.db.User
import io.registration.models.db.tables.UserTable
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery
import scala.concurrent.Future

class UserRepository(db: H2Profile.backend.Database) {
  private val users = TableQuery[UserTable]

  def insert(user: User): Future[Int] = {
    val insertAction = users += user
    db.run(insertAction)
  }

  def findByLogin(login: String): Future[Option[User]] = {
    db.run(users.filter(_.login === login).result.headOption)
  }
}
