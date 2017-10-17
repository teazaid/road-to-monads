package io.monadic.service

import java.time.LocalDate

import cats.data.Kleisli
import cats.instances.future._
import io.monadic.di.Env
import io.registration.models.http.UserRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserValidator {

  type ValidationAction[T] = Kleisli[Future, Env, Either[String, T]]

  def validate(userRequest: UserRequest): ValidationAction[UserRequest] =
    for {
      password <- validatePassword(userRequest)
      age <- validateAge(userRequest)
      login <- validateLogin(userRequest)
      email <- validateEmail(userRequest)
    } yield {
      for {
        _ <- password
        _ <- age
        _ <- email
        l <- login
      } yield l
    }


  private def validatePassword(userRequest: UserRequest): ValidationAction[UserRequest] = Kleisli { _ =>
    if (userRequest.password != userRequest.confirmedPassword)
      Future.successful(Left[String, UserRequest]("Password doesn't equal to confirmed password"))
    else
      Future.successful(Right[String, UserRequest](userRequest))
  }


  private def validateAge(userRequest: UserRequest): ValidationAction[UserRequest] = Kleisli { _ =>
    val now = LocalDate.now
    if (now.getYear - LocalDate.parse(userRequest.birthday).getYear <= 15) {
      Future.successful(Left[String, UserRequest]("User is below 15"))
    } else {
      Future.successful(Right[String, UserRequest](userRequest))
    }
  }

  private def validateLogin(userRequest: UserRequest): ValidationAction[UserRequest] = for {
    env <- Env.all
    userOpt <- env.userRepository.findByLogin(userRequest.login)
  } yield userOpt match {
    case Some(user) => Left[String, UserRequest](s"User ${user.login} is already exists")
    case None => Right[String, UserRequest](userRequest)
  }

  private def validateEmail(userRequest: UserRequest): ValidationAction[UserRequest] = for {
    env <- Env.all
    userOpt <- env.userRepository.findByEmail(userRequest.email)
  } yield userOpt match {
    case Some(user) => Left[String, UserRequest](s"User with email ${user.email} is already exists")
    case None => Right[String, UserRequest](userRequest)
  }


}

