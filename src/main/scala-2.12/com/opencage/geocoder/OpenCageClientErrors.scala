package com.opencage.geocoder

import java.time.Instant

sealed class OpencageClientError(message: String,
                                 cause: Throwable = None.orNull) extends Exception(message, cause)

/**
 * Represents an error while trying to deserialize the response from the server
 * @param cause original error raised by the deserialization framework
 */
class DeserializationError(message: String, cause: io.circe.Error) extends OpencageClientError(message, cause)

/**
 * Represents an error while trying to contact the OpenCage server
 * @param message error message
 * @param cause cause
 */
class UnexpectedError(message: String,
                      cause: Throwable = None.orNull) extends OpencageClientError(message, cause)

/**
 * Thrown when an invalid request is made
 *
 * @param message error message
 */
class InvalidRequestError(message: String) extends OpencageClientError(message)

/**
 * Thrown when a key has exceeded its usage.
 *
 * @param message error message
 */
class QuotaExceededError(message: String) extends OpencageClientError(message)

/**
 * Thrown when a key has exceeded its rate limits.
 *
 * @param message error message
 * @param tryAgainAt instant at which you could try issuing the request again
 */
class RateLimitExceededError(message: String,
                             tryAgainAt: Option[Instant]) extends OpencageClientError(message)

/**
 * Thrown when the server issues a timeout
 *
 * @param message timeout message
 */
class TimeoutError(message: String) extends OpencageClientError(message)

/**
 * Thrown when the auth key isn't accepted
 *
 * @param message timeout message
 */
class ForbiddenError(message: String) extends OpencageClientError(message)

/**
 * Thrown when the request is too long
 *
 * @param message timeout message
 */
class RequestTooLongError(message: String) extends OpencageClientError(message)
