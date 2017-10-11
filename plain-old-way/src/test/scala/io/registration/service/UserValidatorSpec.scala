package io.registration.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
  private val email = "email"

  private val baseUserRequest = UserRequest(login, "1", "1", now.minusYears(16).format(DateTimeFormatter.ISO_LOCAL_DATE), email)

  private val user = User(Some(1), login, "", now, email, UserStatus.Active)

  test("login already exists") {
    val userRepositoryMock = mock(classOf[UserRepository])
    when(userRepositoryMock.findByLogin(login)).thenReturn(Future.successful(Some(user)))
    when(userRepositoryMock.findByEmail(email)).thenReturn(Future.successful(None))

    val userValidator = new UserValidator(userRepositoryMock)
    Await.result(userValidator.validate(baseUserRequest), timeout) match {
      case Left(msg) => assert(msg == s"User ${login} is already exists")
      case _ => fail("user should be created in the db")
    }

    verify(userRepositoryMock).findByLogin(login)
    verify(userRepositoryMock).findByEmail(email)
  }

  test("email already exists") {
    val userRepositoryMock = mock(classOf[UserRepository])
    when(userRepositoryMock.findByLogin(login)).thenReturn(Future.successful(None))
    when(userRepositoryMock.findByEmail(email)).thenReturn(Future.successful(Some(user)))

    val userValidator = new UserValidator(userRepositoryMock)
    Await.result(userValidator.validate(baseUserRequest), timeout) match {
      case Left(msg) => assert(msg == s"User with email ${email} is already exists")
      case _ => fail("user should be created in the db")
    }

    verify(userRepositoryMock).findByLogin(login)
    verify(userRepositoryMock).findByEmail(email)
  }

  test("password is not equal to confirmed password") {
    val userRepositoryMock = mock(classOf[UserRepository])
    when(userRepositoryMock.findByLogin(login)).thenReturn(Future.successful(Some(user)))
    when(userRepositoryMock.findByEmail(email)).thenReturn(Future.successful(None))

    val userValidator = new UserValidator(userRepositoryMock)
    Await.result(userValidator.validate(baseUserRequest.copy(password = "2")), timeout) match {
      case Left(msg) => assert(msg == "Password doesn't equal to confirmed password")
      case _ => fail("Password validation should fail")
    }

    verify(userRepositoryMock).findByLogin(login)
    verify(userRepositoryMock).findByEmail(email)
  }

  test("user is too young") {
    val userRepositoryMock = mock(classOf[UserRepository])
    when(userRepositoryMock.findByLogin(login)).thenReturn(Future.successful(Some(user)))
    when(userRepositoryMock.findByEmail(email)).thenReturn(Future.successful(None))

    val userValidator = new UserValidator(userRepositoryMock)
    val bDay = now.minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
    Await.result(userValidator.validate(baseUserRequest.copy(birthday = bDay)), timeout) match {
      case Left(msg) => assert(msg == "User is below 15")
      case _ => fail("Password validation should fail")
    }

    verify(userRepositoryMock).findByLogin(login)
    verify(userRepositoryMock).findByEmail(email)
  }

  test("validation successful") {
    val userRepositoryMock = mock(classOf[UserRepository])
    when(userRepositoryMock.findByLogin(login)).thenReturn(Future.successful(None))
    when(userRepositoryMock.findByEmail(email)).thenReturn(Future.successful(None))

    val userValidator = new UserValidator(userRepositoryMock)
    Await.result(userValidator.validate(baseUserRequest), timeout) match {
      case Right(actualUserRequest) => assert(actualUserRequest == baseUserRequest)
      case _ => fail("user should be valid")
    }

    verify(userRepositoryMock).findByLogin(login)
    verify(userRepositoryMock).findByEmail(email)
  }
}
