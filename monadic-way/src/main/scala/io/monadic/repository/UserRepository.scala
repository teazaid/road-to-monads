package io.monadic.repository

import cats.data.Reader
import io.registration.models.db.tables.CustomDataTypes._
import io.registration.models.db.tables.UserTable
import io.registration.models.db.{User, UserStatus}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery
import scala.concurrent.Future

class UserRepository {
  private val users = TableQuery[UserTable]

  type DBAction[T] = Reader[H2Profile.backend.Database, Future[T]]

  def insert(user: User): DBAction[Int] = Reader { db =>
    val insertAction = users += user
    db.run(insertAction)
  }

  def findByLogin(login: String): DBAction[Option[User]] = Reader { db =>
    db.run(users.filter(_.login === login).result.headOption)
  }

  def findByEmail(email: String): DBAction[Option[User]] = Reader { db =>
    db.run(users.filter(_.email === email).result.headOption)
  }

  def setStatus(login: String, status: UserStatus.Value): DBAction[Int] = Reader { db =>
    val updateStatusAction = users.filter(_.login === login).map(_.status).update(UserStatus.Active)
    db.run(updateStatusAction)
  }
}
