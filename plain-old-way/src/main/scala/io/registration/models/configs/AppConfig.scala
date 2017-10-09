package io.registration.models.configs

import scala.util.Try

case class AppConfig(confirmationServiceSettings: Try[ConfirmationServiceSettings], dbSettings: Try[DBSettings])
