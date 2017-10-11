package io.registration.models.db.tables

import java.sql.Date
import java.time.LocalDate
import io.registration.models.db.UserStatus
import io.registration.models.db.UserStatus.UserStatus
import slick.jdbc.H2Profile.api._

object CustomDataTypes {
  implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
    l => Date.valueOf(l),
    d => d.toLocalDate
  )

  implicit val userStatusToString = MappedColumnType.base[UserStatus, String](
    l => l.toString,
    d => UserStatus.withName(d)
  )
}
