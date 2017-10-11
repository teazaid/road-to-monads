package io.monadic.route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ValidationRejection
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.monadic.service.{UserConfirmationService, UserService, UserValidator}
import FailFastCirceSupport._
import akka.http.scaladsl.server
import cats.data.Reader
import io.circe.generic.auto._
import io.monadic.repository.UserRepository
import io.registration.models.http.{ConfirmationRequest, UserRequest}
import slick.jdbc.H2Profile

import scala.util.Success


class RegistrationRoute {

  type RegisterAction[T] = Reader[(UserService, UserRepository, H2Profile.backend.DatabaseDef, UserConfirmationService, UserValidator), T]

  val route: RegisterAction[server.Route] = Reader { case (userService, ur, db, uconf, uv) =>
    path("sign-up") {
      post {
        entity(as[UserRequest]) { request =>
          onComplete(userService.register(request).run((ur, db, uconf, uv))) { status =>
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
          onComplete(userService.confirmUser(confirmationRequest).run((ur, db))) { status =>
            status match {
              case Success(_) => complete(HttpEntity(ContentTypes.`application/json`, ""))
              case _ => reject(ValidationRejection("error happened"))
            }
          }
        }
      }
    }
  }

}

