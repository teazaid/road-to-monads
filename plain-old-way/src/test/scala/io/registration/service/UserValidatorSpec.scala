package io.registration.service

import java.time.LocalDate

import scala.concurrent.duration._
import io.registration.models.http.UserRequest
import org.scalatest.FunSuite

import scala.concurrent.Await

class UserValidatorSpec extends FunSuite {
  private val now = LocalDate.now()
  private val timeout = 1.second

  test("login already exists") {

  }

  test("email already exists") {

  }

  test("password is not equal to confirmed password") {
    val userValidator = new UserValidator()
    Await.result(userValidator.validate(UserRequest("login", "1", "2", now.minusYears(16), "test@test.de")), timeout) match {
      case Left(msg) => assert(msg == "Password doesn't equal to confirmed password")
      case _ => fail("Password validation should fail")
    }

  }

  test("is too young") {
    val userValidator = new UserValidator()
    val birthday = now.minusYears(1)
    Await.result(userValidator.validate(UserRequest("login", "1", "1", birthday, "test@test.de")), timeout) match {
      case Left(msg) => assert(msg == "User is below 15")
      case _ => fail("Password validation should fail")
    }
  }

  test("validation successful") {

  }
}
