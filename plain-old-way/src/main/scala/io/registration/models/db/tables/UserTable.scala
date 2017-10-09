package io.registration.models.db.tables

import java.sql.Date
import java.time.LocalDate

import io.registration.models.db.{User, UserStatus}
import io.registration.models.db.UserStatus.UserStatus
import slick.jdbc.H2Profile.api._

case class UserTable(tag: Tag) extends Table[User](tag, "USERS"){

  implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l),
    d => d.toLocalDate
  )

  implicit val userStatusToString = MappedColumnType.base[UserStatus, String](
    l => l.toString,
    d => UserStatus.withName(d)
  )

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def login = column[String]("login", O.Unique)
  def password = column[String]("password")
  def birthday = column[LocalDate]("birthday")
  def email = column[String]("email", O.Unique)
  def status = column[UserStatus]("status")

  def * = (id, login, password, birthday, email, status).mapTo[User]

}
