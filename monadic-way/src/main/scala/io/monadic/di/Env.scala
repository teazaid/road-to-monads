package io.monadic.di

import cats.data.Reader
import com.typesafe.config.ConfigFactory
import io.monadic.repository.UserRepository
import io.monadic.service.{UserConfirmationService, UserService, UserValidator}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.backend.Database

object Env {
  val all = Reader[Env, Env](identity)

  val userRepository = all.map(_.userRepository)
  val userValidator = all.map(_.userValidator)
  val userConfirmationService = all.map(_.userConfirmationService)
  val userService = all.map(_.userService)
  val db = all.map(_.db)
}

trait Env {
  def userRepository: UserRepository = new UserRepository()

  def userValidator: UserValidator = new UserValidator()

  def userConfirmationService: UserConfirmationService = new UserConfirmationService()

  def userService: UserService = new UserService()

  def db: H2Profile.backend.Database = Database.forConfig("local-db", ConfigFactory.parseResources("slick-production.conf"))
}
