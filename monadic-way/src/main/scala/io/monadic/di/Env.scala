package io.monadic.di

import cats.data.Kleisli
import com.typesafe.config.ConfigFactory
import io.monadic.repository.UserRepository
import io.monadic.service.{UserConfirmationService, UserService, UserValidator}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.backend.Database

import scala.concurrent.Future

object Env {
  val all = Kleisli[Future, Env, Env](env => Future.successful(env))
}

trait Env {
  def userRepository: UserRepository = new UserRepository()

  def userValidator: UserValidator = new UserValidator()

  def userConfirmationService: UserConfirmationService = new UserConfirmationService()

  def userService: UserService = new UserService()

  def db: H2Profile.backend.Database = Database.forConfig("local-db", ConfigFactory.parseResources("slick-production.conf"))
}
