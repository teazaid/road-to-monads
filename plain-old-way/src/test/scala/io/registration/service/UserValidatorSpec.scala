package io.registration.service

import java.time.LocalDate

import io.registration.models.db.{User, UserStatus}
import io.registration.models.http.UserRequest
import io.registration.repository.UserRepository
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FunSuite

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class UserValidatorSpec extends FunSuite {

  private val now = LocalDate.now()
  private val timeout = 5.second

  private val login = "login"
  private val baseUserRequest = UserRequest(login, "1", "1", now.minusYears(16), "test@test.de")

  private val user = User(Some(1), login, "", now, "", UserStatus.Active)

  test("login already exists") {
    val userRepositoryMock = mock(classOf[UserRepository])
    when(userRepositoryMock.findByLogin(login)).thenReturn(Future.successful(Some(user)))

    val userValidator = new UserValidator(userRepositoryMock)
    Await.result(userValidator.validate(baseUserRequest), timeout) match {
      case Left(msg) => assert(msg == s"User ${login} already exists")
      case _ => fail("user should be created in the db")
    }

    verify(userRepositoryMock).findByLogin(login)
  }

  test("email already exists") {

  }

  test("password is not equal to confirmed password") {
    val userRepositoryMock = mock(classOf[UserRepository])
    when(userRepositoryMock.findByLogin(login)).thenReturn(Future.successful(Some(user)))

    val userValidator = new UserValidator(userRepositoryMock)
    Await.result(userValidator.validate(baseUserRequest.copy(password = "2")), timeout) match {
      case Left(msg) => assert(msg == "Password doesn't equal to confirmed password")
      case _ => fail("Password validation should fail")
    }

    verify(userRepositoryMock).findByLogin(anyString)
  }

  test("is too young") {
    val userRepositoryMock = mock(classOf[UserRepository])
    when(userRepositoryMock.findByLogin(login)).thenReturn(Future.successful(Some(user)))

    val userValidator = new UserValidator(userRepositoryMock)
    val bDay = now.minusYears(1)
    Await.result(userValidator.validate(baseUserRequest.copy(birthday = bDay)), timeout) match {
      case Left(msg) => assert(msg == "User is below 15")
      case _ => fail("Password validation should fail")
    }

    verify(userRepositoryMock).findByLogin(anyString)
  }

  test("validation successful") {

  }
}
