package io.registration.service

import java.time.LocalDate

import io.registration.models.http.UserRequest
import io.registration.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserValidator(userRepository: UserRepository) {

  def validate(userRequest: UserRequest): Future[Either[String, UserRequest]] = {
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
  }

  private def validatePassword(userRequest: UserRequest): Future[Either[String, UserRequest]] =
    if (userRequest.password != userRequest.confirmedPassword)
      Future.successful(Left[String, UserRequest]("Password doesn't equal to confirmed password"))
    else
      Future.successful(Right[String, UserRequest](userRequest))


  private def validateAge(userRequest: UserRequest): Future[Either[String, UserRequest]] = {
    val now = LocalDate.now
    if (now.getYear - userRequest.birthday.getYear <= 15) {
      Future.successful(Left[String, UserRequest]("User is below 15"))
    } else {
      Future.successful(Right[String, UserRequest](userRequest))
    }
  }

  private def validateLogin(userRequest: UserRequest): Future[Either[String, UserRequest]] =
    userRepository.findByLogin(userRequest.login).map { userOpt =>
      userOpt match {
        case Some(user) => Left[String, UserRequest](s"User ${user.login} is already exists")
        case None => Right[String, UserRequest](userRequest)
      }
    }

  private def validateEmail(userRequest: UserRequest): Future[Either[String, UserRequest]] =
    userRepository.findByEmail(userRequest.email).map { userOpt =>
      userOpt match {
        case Some(user) => Left[String, UserRequest](s"User with email ${user.email} is already exists")
        case None => Right[String, UserRequest](userRequest)
      }
    }

}
