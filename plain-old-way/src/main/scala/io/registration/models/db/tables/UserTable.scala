package io.registration.models.db.tables

import java.time.LocalDate
import io.registration.models.db.User
import io.registration.models.db.UserStatus.UserStatus
import slick.jdbc.H2Profile.api._
import io.registration.models.db.tables.CustomDataTypes._

case class UserTable(tag: Tag) extends Table[User](tag, "USERS"){
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def login = column[String]("login", O.Unique)
  def password = column[String]("password")
  def birthday = column[LocalDate]("birthday")
  def email = column[String]("email", O.Unique)
  def status = column[UserStatus]("status")

  def * = (id, login, password, birthday, email, status).mapTo[User]

}
