package io.registration.service

import scala.concurrent.Future

trait UserConfirmationService {
  def confirmUser(login: String, email: String): Future[String]
}
