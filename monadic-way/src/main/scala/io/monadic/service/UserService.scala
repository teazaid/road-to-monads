package io.monadic.service

import java.time.LocalDate

import cats.data.Kleisli
import cats.instances.future._
import io.monadic.di.Env
import io.registration.models.db.{User, UserStatus}
import io.registration.models.http.{ConfirmationRequest, UserRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService {
  type UserServiceAction[T] = Kleisli[Future, Env, T]

  def confirmUser(confirmationRequest: ConfirmationRequest): UserServiceAction[Unit] = for {
    env <- Env.all
    _ <- env.userRepository.setStatus(confirmationRequest.login, UserStatus.Active)
  } yield ()

  def register(userRequest: UserRequest): UserServiceAction[String] = {
    val validationResultF = for {
      env <- Env.all
      validationResult <- env.userValidator.validate(userRequest)
    } yield validationResult match {
      case Left(msg) => throw new Exception(msg)
      case Right(userRequest) => userRequest
    }

    for {
      env <- Env.all
      validatedUser <- validationResultF
      _ <- env.userRepository.insert(User(None,
        validatedUser.login,
        validatedUser.password,
        LocalDate.parse(validatedUser.birthday),
        validatedUser.email,
        UserStatus.NonActive
      ))
      confirmation <- Kleisli.lift(env.userConfirmationService.sendConfirmationEmail(userRequest.login, userRequest.email))
    } yield confirmation
  }
}

