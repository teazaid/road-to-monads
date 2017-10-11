package io.registration

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import io.registration.repository.UserRepository
import io.registration.route.RegistrationRoute
import io.registration.service.{UserConfirmationService, UserService, UserValidator}
import slick.jdbc.H2Profile.backend.Database
import scala.io.StdIn

object Boot {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future map/flatmap in the end
    implicit val executionContext = system.dispatcher

    val db = Database.forConfig("local-db", ConfigFactory.parseResources("slick-production.conf"))

    val userRepository = new UserRepository(db)
    val userValidator = new UserValidator(userRepository)
    val userConfirmationService = new UserConfirmationService()
    val userService = new UserService(userConfirmationService, userRepository, userValidator)
    val route = new RegistrationRoute(userService).route

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate())
  }
}
