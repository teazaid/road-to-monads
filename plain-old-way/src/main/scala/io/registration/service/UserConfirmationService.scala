package io.registration.service

import scala.concurrent.Future

class UserConfirmationService {
  def confirmUser(login: String, email: String): Future[String] = {
    Future.successful(login + email)
  }
}
