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
import io.monadic.di.Env
import io.registration.models.http.{ConfirmationRequest, UserRequest}

import scala.util.Success


class RegistrationRoute {
  type RegisterAction[T] = Reader[Env, T]

  val route: RegisterAction[server.Route] = Reader { env =>
    path("sign-up") {
      post {
        entity(as[UserRequest]) { request =>
          onComplete(env.userService.register(request).run(env)) { status =>
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
          onComplete(env.userService.confirmUser(confirmationRequest).run(env)) { result =>
            result match {
              case Success(_) => complete(HttpEntity(ContentTypes.`application/json`, ""))
              case _ => reject(ValidationRejection("error happened"))
            }
          }
        }
      }
    }
  }
}

