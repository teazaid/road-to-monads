package io.registration.repository

import java.time.LocalDate
import com.typesafe.config.ConfigFactory
import io.registration.models.db.tables.UserTable
import io.registration.models.db.{User, UserStatus}
import org.scalatest.FunSuite
import slick.jdbc.H2Profile.api._
import slick.jdbc.H2Profile.backend.Database
import slick.lifted.TableQuery
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class UserRepositorySpec extends FunSuite {
  private val timeout = 5.seconds
  private val db = Database.forConfig("local-db", ConfigFactory.parseResources("slick-local.conf"))
  private val login = "login"
  private val UserToInsert = User(None, login, "2312", LocalDate.now(), "email@s.rt", UserStatus.NonActive)
  private val userRepository = new UserRepository(db)

  test("populate db and get data") {
    val users = TableQuery[UserTable]

    val findResultF = for {
      _ <- db.run(users.schema.create)
      _ <- userRepository.insert(UserToInsert)
      findResult <- userRepository.findByLogin(login)
    } yield findResult

    val user = Await.result(findResultF, timeout)
    assert(user.get == UserToInsert.copy(id = Some(1)))
  }

  test("activate user") {
    val userOpt = Await.result(userRepository.findByLogin(login), timeout)
    assert(userOpt.get == UserToInsert.copy(id = Some(1)))

    val updatedUserActionF = for {
      _ <- userRepository.setStatus(userOpt.get.login, UserStatus.Active)
      updatedUserAction <- userRepository.findByLogin(login)
    } yield updatedUserAction

    val updatedUserOpt = Await.result(updatedUserActionF, timeout)
    assert(updatedUserOpt.get == UserToInsert.copy(id = Some(1), status = UserStatus.Active))
  }
}


