package io.registration.service

import java.time.LocalDate
import io.registration.models.db.{User, UserStatus}
import io.registration.models.http.{ConfirmationRequest, UserRequest}
import io.registration.repository.UserRepository
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService(userConfirmationService: UserConfirmationService,
                  userRepository: UserRepository,
                  userValidator: UserValidator) {
  def confirmUser(confirmationRequest: ConfirmationRequest): Future[Unit] = {
    userRepository.setStatus(confirmationRequest.login, UserStatus.Active).map(_ => ())
  }

  def register(userRequest: UserRequest): Future[String] = {
    val validationResultF = userValidator.validate(userRequest).map { validateResult =>
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
      ))
      confirmation <- userConfirmationService.sendConfirmationEmail(userRequest.login, userRequest.email)
    } yield confirmation
  }
}
