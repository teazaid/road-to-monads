package io.registration.models.http

case class UserRequest(login: String,
                       password: String,
                       confirmedPassword: String,
                       birthday: String,
                       email: String)
