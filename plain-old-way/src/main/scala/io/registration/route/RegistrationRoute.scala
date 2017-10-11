package io.registration.route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ValidationRejection
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.registration.service.UserService

import scala.util.Success
import FailFastCirceSupport._
import io.circe.generic.auto._
import io.registration.models.http.{ConfirmationRequest, UserRequest}

class RegistrationRoute(userService: UserService) {

  val route = path("sign-up") {
    post {
      entity(as[UserRequest]) { request =>
        onComplete(userService.register(request)) { status =>
          status match {
            case Success(msg) => complete(HttpEntity(ContentTypes.`application/json`, msg))
            case _ => reject(ValidationRejection("error happened"))
          }
        }
      }
    }
  } ~ path("confirm") {
    get {
      parameters(('login, 'token)).as(ConfirmationRequest) { confirmationRequest =>
        onComplete(userService.confirmUser(confirmationRequest)) { status =>
          status match {
            case Success(_) => complete(HttpEntity(ContentTypes.`application/json`, ""))
            case _ => reject(ValidationRejection("error happened"))
          }
        }
      }
    }
  }

}
