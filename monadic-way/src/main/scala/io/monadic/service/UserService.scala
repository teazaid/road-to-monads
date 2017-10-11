package io.monadic.service

import java.time.LocalDate

import cats.data.Reader
import io.registration.models.db.{User, UserStatus}
import io.registration.models.http.{ConfirmationRequest, UserRequest}
import io.monadic.repository.UserRepository
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService {

  type ConfirmUserAction[T] = Reader[(UserRepository, H2Profile.backend.DatabaseDef), T]
  type RegisterAction[T] = Reader[(UserRepository, H2Profile.backend.DatabaseDef, UserConfirmationService, UserValidator), T]

  def confirmUser(confirmationRequest: ConfirmationRequest): ConfirmUserAction[Future[Unit]] = Reader { case (userRepository, db) =>
    userRepository.setStatus(confirmationRequest.login, UserStatus.Active).run(db).map(_ => ())
  }

  def register(userRequest: UserRequest): RegisterAction[Future[String]] = Reader { case (userRepository, db, userConfirmationService, userValidator) =>
    val validationResultF = userValidator.validate(userRequest).run(userRepository, db).map { validateResult =>
      validateResult match {
        case Left(msg) => throw new Exception(msg)
        case Right(userRequest) => userRequest
      }
    }

    for {
      validatedUser <- validationResultF
      _ <- userRepository.insert(User(None,
        validatedUser.login,
        validatedUser.password,
        LocalDate.parse(validatedUser.birthday),
        validatedUser.email,
        UserStatus.NonActive
      )).run(db)
      confirmation <- userConfirmationService.sendConfirmationEmail(userRequest.login, userRequest.email)
    } yield confirmation
  }
}

