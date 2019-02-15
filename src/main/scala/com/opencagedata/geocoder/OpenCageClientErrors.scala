package com.opencagedata.geocoder

import java.time.Instant

abstract sealed class OpencageClientError(
  message: String,
  cause:   Throwable = None.orNull
) extends Exception(message, cause)

/**
 * Represents an error while trying to deserialize the response from the server
 * @param cause original error raised by the deserialization framework
 */
case class DeserializationError(
  message: String,
  cause:   io.circe.Error
) extends OpencageClientError(message, cause)

/**
 * Represents an error while trying to contact the OpenCage server
 * @param message error message
 * @param cause cause
 */
case class UnexpectedError(
  message: String,
  cause:   Throwable = None.orNull
) extends OpencageClientError(message, cause)

/**
 * Thrown when an invalid request is made
 *
 * @param message error message
 */
case class InvalidRequestError(message: String) extends OpencageClientError(message)

/**
 * Thrown when a key has exceeded its usage.
 *
 * @param message error message
 */
case class QuotaExceededError(message: String) extends OpencageClientError(message)

/**
 * Thrown when a key has exceeded its rate limits.
 *
 * @param message error message
 * @param tryAgainAt instant at which you could try issuing the request again
 */
case class RateLimitExceededError(
  message:    String,
  tryAgainAt: Option[Instant]
) extends OpencageClientError(message)

/**
 * Thrown when the server issues a timeout
 *
 * @param message timeout message
 */
case class TimeoutError(message: String) extends OpencageClientError(message)

/**
 * Thrown when the auth key isn't accepted
 *
 * @param message timeout message
 */
case class ForbiddenError(message: String) extends OpencageClientError(message)

/**
 * Thrown when the request is too long
 *
 * @param message timeout message
 */
case class RequestTooLongError(message: String) extends OpencageClientError(message)
