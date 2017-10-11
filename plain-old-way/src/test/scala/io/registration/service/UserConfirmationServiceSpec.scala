package io.registration.service

import org.scalatest.FunSuite
import scala.concurrent.duration._
import scala.concurrent.Await

class UserConfirmationServiceSpec extends FunSuite {
  private val timeout = 2.seconds
  private val login = "login"
  private val email = "email"

  test("confirmUser should return confirmation token") {
    val userConfirmationService = new UserConfirmationService()
    assert(Await.result(userConfirmationService.sendConfirmationEmail(login, email), timeout) == (login + email))
  }

}
