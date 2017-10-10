package io.registration.service

import java.time.LocalDate

import io.registration.models.db.{User, UserStatus}
import io.registration.models.db.UserStatus.UserStatus
import io.registration.models.http.{ConfirmationRequest, UserRequest}
import io.registration.repository.UserRepository
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FunSuite

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

class UserServiceSpec extends FunSuite {
  private val timeout = 2.seconds
  private val now = LocalDate.now
  private val login = "login"
  private val email = "email"
  private val userRequest = UserRequest(login, "password", "password", now, email)
  private val confirmationToken = "confirmationToken"
  private val confirmationRequest = ConfirmationRequest(login, confirmationToken)

  private val successfulConfirmation = "done"

  test("register valid user") {

    val userRepositoryMock = mock(classOf[UserRepository])
    val userValidatorMock = mock(classOf[UserValidator])
    val userConfirmationServiceMock = mock(classOf[UserConfirmationService])

    when(userValidatorMock.validate(userRequest)).thenReturn(Future.successful(Right[String, UserRequest](userRequest)))
    when(userRepositoryMock.insert(any(classOf[User]))).thenReturn(Future.successful(1))
    when(userConfirmationServiceMock.confirmUser(userRequest.login, userRequest.email)).thenReturn(Future.successful(successfulConfirmation))

    val userService = new UserService(userConfirmationServiceMock,
      userRepositoryMock,
      userValidatorMock)

    val registeredUserF = userService.register(userRequest)
    val registeredUser = Await.result(registeredUserF, timeout)

    assert(registeredUser == successfulConfirmation)

    verify(userValidatorMock).validate(any[UserRequest])
    verify(userRepositoryMock).insert(any[User])
    verify(userConfirmationServiceMock).confirmUser(login, email)
  }

  test("confirm user") {
    val userRepositoryMock = mock(classOf[UserRepository])
    val userValidatorMock = mock(classOf[UserValidator])
    val userConfirmationServiceMock = mock(classOf[UserConfirmationService])

    val userService = new UserService(userConfirmationServiceMock,
      userRepositoryMock,
      userValidatorMock)

    when(userRepositoryMock.setStatus(confirmationRequest.login, UserStatus.Active)).thenReturn(Future.successful(1))

    val confirmationUserF = userService.confirmUser(confirmationRequest)
    Await.result(confirmationUserF, timeout)

    verify(userValidatorMock, times(0)).validate(any[UserRequest])
    verify(userRepositoryMock, times(0)).insert(any[User])
    verify(userRepositoryMock, times(1)).setStatus(anyString, any[UserStatus])
  }

  test("register invalid user") {

    val userRepositoryMock = mock(classOf[UserRepository])
    val userValidatorMock = mock(classOf[UserValidator])
    val userConfirmationServiceMock = mock(classOf[UserConfirmationService])

    when(userValidatorMock.validate(userRequest)).thenReturn(Future.successful(Left[String, UserRequest]("Invalid age")))

    val userService = new UserService(userConfirmationServiceMock,
      userRepositoryMock,
      userValidatorMock)

    val registeredUserF = userService.register(userRequest)
    assert(Try(Await.result(registeredUserF, timeout)).isFailure)

    verify(userValidatorMock).validate(any[UserRequest])
    verify(userRepositoryMock, times(0)).insert(any[User])
    verify(userConfirmationServiceMock, times(0)).confirmUser(anyString, anyString)
  }

  test("register valid user with insertion failure") {

    val userRepositoryMock = mock(classOf[UserRepository])
    val userValidatorMock = mock(classOf[UserValidator])
    val userConfirmationServiceMock = mock(classOf[UserConfirmationService])

    when(userValidatorMock.validate(userRequest)).thenReturn(Future.successful(Right[String, UserRequest](userRequest)))
    when(userRepositoryMock.insert(any(classOf[User]))).thenReturn(Future.failed(new Exception("failed to insert user")))
    when(userConfirmationServiceMock.confirmUser(userRequest.login, userRequest.email)).thenReturn(Future.successful(successfulConfirmation))

    val userService = new UserService(userConfirmationServiceMock,
      userRepositoryMock,
      userValidatorMock)

    val registeredUserF = userService.register(userRequest)
    assert(Try(Await.result(registeredUserF, timeout)).isFailure)

    verify(userValidatorMock).validate(any[UserRequest])
    verify(userRepositoryMock).insert(any[User])
    verify(userConfirmationServiceMock, times(0)).confirmUser(anyString, anyString)
  }

  test("register valid user with failed confirmation") {

    val userRepositoryMock = mock(classOf[UserRepository])
    val userValidatorMock = mock(classOf[UserValidator])
    val userConfirmationServiceMock = mock(classOf[UserConfirmationService])

    when(userValidatorMock.validate(userRequest)).thenReturn(Future.successful(Right[String, UserRequest](userRequest)))
    when(userRepositoryMock.insert(any(classOf[User]))).thenReturn(Future.successful(1))
    when(userConfirmationServiceMock.confirmUser(userRequest.login, userRequest.email)).thenReturn(Future.failed(new Exception("failed to sent")))

    val userService = new UserService(userConfirmationServiceMock,
      userRepositoryMock,
      userValidatorMock)

    val registeredUserF = userService.register(userRequest)
    assert(Try(Await.result(registeredUserF, timeout)).isFailure)

    verify(userValidatorMock).validate(any[UserRequest])
    verify(userRepositoryMock).insert(any[User])
    verify(userConfirmationServiceMock).confirmUser(anyString, anyString)
  }
}
