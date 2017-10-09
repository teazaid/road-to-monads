package io.registration.models.db

import java.time.LocalDate

import io.registration.models.db.UserStatus.UserStatus

case class User(id: Option[Long],
                login: String,
                password: String,
                birthday: LocalDate,
                email: String,
                status: UserStatus)