package io.monadic.service

import java.time.LocalDate

import cats.data.Reader
import io.monadic.di.Env
import io.registration.models.db.{User, UserStatus}
import io.registration.models.http.{ConfirmationRequest, UserRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService {
  type UserServiceAction[T] = Reader[Env, T]

  def confirmUser(confirmationRequest: ConfirmationRequest): UserServiceAction[Future[Unit]] = Reader { env =>
    env.userRepository.setStatus(confirmationRequest.login, UserStatus.Active).run(env).map(_ => ())
  }


  def register(userRequest: UserRequest): UserServiceAction[Future[String]] = Reader { env =>
    val validationResultF = env.userValidator.validate(userRequest).run(env).map { validateResult =>
      validateResult match {
        case Left(msg) => throw new Exception(msg)
        case Right(userRequest) => userRequest
      }
    }

    for {
      validatedUser <- validationResultF
      _ <- env.userRepository.insert(User(None,
        validatedUser.login,
        validatedUser.password,
        LocalDate.parse(validatedUser.birthday),
        validatedUser.email,
        UserStatus.NonActive
      )).run(env)
      confirmation <- env.userConfirmationService.sendConfirmationEmail(userRequest.login, userRequest.email)
    } yield confirmation
  }
}

