package io.registration.config

import com.typesafe.config.Config
import io.registration.models.configs.{AppConfig, ConfirmationServiceSettings, DBSettings}

import scala.util.Try

class ConfigurationManager(config: Config) {
  def getConfig(): Try[AppConfig] = for {
    confirmationSettings <- getConfigPath("confirmation")
    dbSettings <- getConfigPath("db")
  } yield AppConfig(parseConfirmationServiceSettings(confirmationSettings), parseDbSettings(dbSettings))

  private def parseConfirmationServiceSettings(confirmationConfig: Config): Try[ConfirmationServiceSettings] = for {
    url <- readString(confirmationConfig, "url")
    login <- readString(confirmationConfig, "login")
    password <- readString(confirmationConfig, "password")
  } yield ConfirmationServiceSettings(url, login, password)

  private def parseDbSettings(dbConfig: Config): Try[DBSettings] = for {
    url <- readString(dbConfig, "url")
    dbName <- readString(dbConfig, "dbName")
    login <- readString(dbConfig, "login")
    password <- readString(dbConfig, "password")
  } yield DBSettings(url, dbName, login, password)

  private def getConfigPath(path: String): Try[Config] = {
    Try(config.getConfig(path))
  }

  private def readString(config: Config, path: String): Try[String] = {
    Try(config.getString(path))
  }
}
