package io.registration.service

import java.time.LocalDate

import io.registration.models.http.UserRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserValidator {
  def validate(userRequest: UserRequest): Future[Either[String, UserRequest]] = {
    for {
      password <- validatePassword(userRequest)
      age <- validateAge(userRequest)
    } yield {
      for {
        _ <- password
        a <- age
      } yield a
    }
  }

  private def validatePassword(userRequest: UserRequest): Future[Either[String, UserRequest]] = {
    if (userRequest.password != userRequest.confirmedPassword)
      Future.successful(Left[String, UserRequest]("Password doesn't equal to confirmed password"))
    else
      Future.successful(Right[String, UserRequest](userRequest))
  }

  private def validateAge(userRequest: UserRequest): Future[Either[String, UserRequest]] = {
    val now = LocalDate.now
    if (now.getYear - userRequest.birthday.getYear <= 15) {
      Future.successful(Left[String, UserRequest]("User is below 15"))
    } else {
      Future.successful(Right[String, UserRequest](userRequest))
    }
  }
}
