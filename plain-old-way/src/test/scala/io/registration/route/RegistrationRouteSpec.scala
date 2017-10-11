package io.registration.route

import java.time.LocalDate

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import io.registration.models.http.{ConfirmationRequest, UserRequest}
import io.registration.service.UserService
import org.scalatest.{Matchers, WordSpec}
import org.mockito.Mockito._
import org.mockito.Matchers._

import scala.concurrent.Future
import io.circe.generic.auto._
import io.circe.syntax._

class RegistrationRouteSpec extends WordSpec with Matchers with ScalatestRouteTest {

  private val userRequest = UserRequest("test", "1", "1", "2017-01-10", "")

  "RegistrationRoute" should {
    "register a new user" in {
      val userServiceMock = mock(classOf[UserService])
      when(userServiceMock.register(any[UserRequest])).thenReturn(Future.successful("done"))

      val route = new RegistrationRoute(userServiceMock)
      val registrationRequest = ByteString(userRequest.asJson.noSpaces)

      Post("/sign-up", HttpEntity(MediaTypes.`application/json`, registrationRequest)) ~> route.route ~> check {
        status.intValue() shouldEqual 200
        responseAs[String] shouldEqual "done"
      }

      verify(userServiceMock).register(userRequest)
    }

    "confirm registered user" in {
      val confirmationRequest = ConfirmationRequest("user123", "1234")
      val userServiceMock = mock(classOf[UserService])
      when(userServiceMock.confirmUser(any[ConfirmationRequest])).thenReturn(Future.successful(()))

      val route = new RegistrationRoute(userServiceMock)

      Get("/confirm?login=user123&token=1234") ~> route.route ~> check {
        status.intValue() shouldEqual 200
      }

      verify(userServiceMock).confirmUser(confirmationRequest)
    }
  }
}
