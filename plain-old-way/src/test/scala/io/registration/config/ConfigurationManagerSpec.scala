package io.registration.config

import com.typesafe.config.ConfigFactory
import io.registration.models.configs.{AppConfig, ConfirmationServiceSettings, DBSettings}
import org.scalatest.FunSuite

import scala.util.{Failure, Success}

class ConfigurationManagerSpec extends FunSuite {
  private val dbConfig = DBSettings("dbUrl", "dbName", "dbUser", "dbPassword")
  private val confirmationServiceSettings = ConfirmationServiceSettings("serviceUrl", "login", "password")

  test("ConfigurationManager couldn't load config") {
    assert(new ConfigurationManager(ConfigFactory.empty()).getConfig().isFailure)
  }

  test("ConfigurationManager loads config") {
    val expectedConfig = AppConfig(Success(confirmationServiceSettings), Success(dbConfig))

    val configurationManager = new ConfigurationManager(ConfigFactory.parseResources("fully-specified.conf"))

    val appConfig = configurationManager.getConfig()
    assert(appConfig.isSuccess)
    assert(appConfig.get == expectedConfig)
  }

  test("ConfigurationManager fails if cant find dbConfig") {
    val configurationManager = new ConfigurationManager(ConfigFactory.parseResources("confirmation-only.conf"))
    assert(configurationManager.getConfig().isFailure)
  }

  test("ConfigurationManager fails if cant find confirmation configs") {
    val configurationManager = new ConfigurationManager(ConfigFactory.parseResources("db-settings-only.conf"))
    assert(configurationManager.getConfig().isFailure)
  }

  test("ConfigurationManager loads invalid dbSettings") {
    val configurationManager = new ConfigurationManager(ConfigFactory.parseResources("broken-dbsettings.conf"))
    val appConfig = configurationManager.getConfig()
    appConfig match {
      case Success(AppConfig(Success(confirm), Failure(_))) => assert(confirm == confirmationServiceSettings)
      case _ => fail("shouldn't reach this clause")
    }
  }

  test("ConfigurationManager loads invalid confirmation settings") {
    val configurationManager = new ConfigurationManager(ConfigFactory.parseResources("broken-confirmation-settings.conf"))
    val appConfig = configurationManager.getConfig()
    appConfig match {
      case Success(AppConfig(Failure(_), Success(confirm))) => assert(confirm == dbConfig)
      case _ => fail("shouldn't reach this clause")
    }
  }
}
