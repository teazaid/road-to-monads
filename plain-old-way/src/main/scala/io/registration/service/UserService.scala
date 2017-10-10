package io.registration.service

import io.registration.models.db.{User, UserStatus}
import io.registration.models.http.UserRequest
import io.registration.repository.UserRepository
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService(userConfirmationService: UserConfirmationService,
                  userRepository: UserRepository,
                  userValidator: UserValidator) {

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
        validatedUser.birthday,
        validatedUser.email,
        UserStatus.NonActive
      ))
      confirmation <- userConfirmationService.confirmUser(userRequest.login, userRequest.email)
    } yield confirmation
  }
}
