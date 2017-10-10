package io.registration.models.http

import java.time.LocalDate

case class UserRequest(login: String,
                       password: String,
                       confirmedPassword: String,
                       birthday: LocalDate,
                       email: String)
