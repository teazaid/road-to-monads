package io.registration.models.db

object UserStatus extends Enumeration {
  type UserStatus = Value
  val Active, NonActive = Value
}
