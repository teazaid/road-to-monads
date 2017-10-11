package io.monadic.service

import java.time.LocalDate

import cats.data.Reader
import io.registration.models.http.UserRequest
import io.monadic.repository.UserRepository
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserValidator {

  type ValidationAction[T] = Reader[(UserRepository, H2Profile.backend.Database), T]

  def validate(userRequest: UserRequest): ValidationAction[Future[Either[String, UserRequest]]] = Reader { case (userRepository, db) =>
    for {
      password <- validatePassword(userRequest)
      age <- validateAge(userRequest)
      login <- validateLogin(userRequest).run((userRepository, db))
      email <- validateEmail(userRequest).run((userRepository, db))
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
    if (now.getYear - LocalDate.parse(userRequest.birthday).getYear <= 15) {
      Future.successful(Left[String, UserRequest]("User is below 15"))
    } else {
      Future.successful(Right[String, UserRequest](userRequest))
    }
  }

  private def validateLogin(userRequest: UserRequest): ValidationAction[Future[Either[String, UserRequest]]] = Reader { case (userRepository, db) =>
    userRepository.findByLogin(userRequest.login).run(db).map { userOpt =>
      userOpt match {
        case Some(user) => Left[String, UserRequest](s"User ${user.login} is already exists")
        case None => Right[String, UserRequest](userRequest)
      }
    }
  }

  private def validateEmail(userRequest: UserRequest): ValidationAction[Future[Either[String, UserRequest]]] = Reader { case (userRepository, db) =>
    userRepository.findByEmail(userRequest.email).run(db).map { userOpt =>
      userOpt match {
        case Some(user) => Left[String, UserRequest](s"User with email ${user.email} is already exists")
        case None => Right[String, UserRequest](userRequest)
      }
    }
  }

}

