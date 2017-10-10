package io.registration.models.http

case class ConfirmationRequest(login: String, confirmationToken: String)
