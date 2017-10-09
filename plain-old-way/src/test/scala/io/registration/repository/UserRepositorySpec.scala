package io.registration.repository

import java.time.LocalDate

import com.typesafe.config.ConfigFactory
import io.registration.models.db.tables.UserTable
import io.registration.models.db.{User, UserStatus}
import org.scalatest.FunSuite
import slick.jdbc.H2Profile.backend.Database
import slick.lifted.TableQuery
import slick.jdbc.H2Profile.api._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class UserRepositorySpec extends FunSuite {
  private val timeout = 2.seconds
  private val db = Database.forConfig("local-db", ConfigFactory.parseResources("slick-local.conf"))
  private val login = "login"
  private val UserToInsert = User(None, login, "2312", LocalDate.now(), "email@s.rt", UserStatus.Active)

  test("populate db and get data") {
    val users = TableQuery[UserTable]
    val userRepository = new UserRepository(db)

    val findResultF = for {
      _ <- db.run(users.schema.create)
      _ <- userRepository.insert(UserToInsert)
      findResult <- userRepository.findByLogin(login)
    } yield findResult

    val user = Await.result(findResultF, timeout)
    assert(user.get == UserToInsert.copy(id = Some(1)))
  }
}


