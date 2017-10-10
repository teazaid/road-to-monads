package io.registration.service

import io.registration.models.http.UserRequest

import scala.concurrent.Future

trait UserValidator {
  def validate(user: UserRequest): Future[Either[String, UserRequest]]
}
