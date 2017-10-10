package io.registration.repository

import io.registration.models.db.tables.CustomDataTypes._
import io.registration.models.db.tables.UserTable
import io.registration.models.db.{User, UserStatus}
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

  def setStatus(login: String, status: UserStatus.Value): Future[Int] = {
    val updateStatusAction = users.filter(_.login === login).map(_.status).update(UserStatus.Active)
    db.run(updateStatusAction)
  }
}
