package com.github.nmdguerreiro.opencage.geocoder

import com.github.nmdguerreiro.opencage.geocoder.OpenCageClient.Scheme.Scheme
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import com.softwaremill.sttp.circe._
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Creates a new client for the OpenCage forward and reverse geocoders.
 *
 * The client auto-closes its resources if used within a try-with-resources block. Otherwise, you should call close
 * when you're done with it to avoid resource leakage.
 *
 * @param authKey The auth key provided to you by OpenCage
 * @param scheme http/https
 * @param hostname Defaults to com.github.nmdguerreiro.opencage.geocoder.OpenCageClient#defaultHostname(), but can be
  *                 overridden for testing purposes
 * @param backend The AHC backend is used by default. If you need to configure proxies, timeouts, connection pools,
 *                etc, you can do that yourself, by providing a custom backend. That can easily be done
 *                by calling com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend#apply(com.softwaremill.sttp.SttpBackendOptions,
 *                scala.concurrent.ExecutionContext) and/or by supplying it a custom configuration
 *                (com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend#usingConfig(org.asynchttpclient.AsyncHttpClientConfig,
 *                scala.concurrent.ExecutionContext)) before calling this constructor.
 */
class OpenCageClient(authKey: String,
                     scheme: Scheme = OpenCageClient.defaultScheme,
                     hostname: String = OpenCageClient.defaultHostname,
                     port: Int = OpenCageClient.defaultPort,
                     executionContext: ExecutionContext = ExecutionContext.global,
                     backend: SttpBackend[Future, Nothing] = OpenCageClient.defaultBackend,
                     userAgent: String = OpenCageClient.defaultUserAgent) extends AutoCloseable {

  implicit val backendInUse: SttpBackend[Future, Nothing] = backend

  /**
   * Reverse geocodes a position (i.e. converts a latitude/longitude to a set of identifiers and codes that represent that position).
   * @param lat latitude
   * @param lng longitude
   * @param params Allows you to specify additional parameters to the call. @see com.github.nmdguerreiro.opencage.geocoder.OpenCageClientParams
   * @return all the information available for that position, including some metadata regarding the execution of the request
   */
  def reverseGeocode(lat: Float,
                     lng: Float,
                     params: OpenCageClientParams = OpenCageClientParams()): Future[OpenCageResponse] = {
    doCall(s"$lat,$lng", params)
  }

  /**
   * Forward geocodes an address (i.e. converts a textual address to a set of positions and returns information about those positions).
   * @param placeOrAddress place or address to lookup (e.g. Branderburg Gate)
   * @param params Allows you to specify additional parameters to the call. @see com.github.nmdguerreiro.opencage.geocoder.OpenCageClientParams
   * @return all the information available for the positions that match the address, including some metadata regarding the execution of the request
   */
  def forwardGeocode(placeOrAddress: String,
                     params: OpenCageClientParams = OpenCageClientParams()): Future[OpenCageResponse] = {
    doCall(placeOrAddress, params)
  }

  private def doCall(query: String, params: OpenCageClientParams): Future[OpenCageResponse] = {
    val uri = buildUri(query, params)
    val request = sttp.get(uri).header(HeaderNames.UserAgent, userAgent)

    val response = request.response(asJson[OpenCageResponse]).send()

    response.transform {
      case Success(res) => handleHttpRequestSuccess(res)
      case Failure(ex) => Failure(new UnexpectedError(s"Unexpected error: ${ex}.", ex))
    }(executionContext)
  }

  private def buildUri(query: String, withAnnotations: Boolean): Uri = {

    val uri = uri"${scheme.toString}://$hostname:$port/geocode/v1/json".params(
      Map(
        "q" -> query,
        "key" -> authKey
      )
    )

    val finalUri = if (withAnnotations) uri else uri.param("no_annotations", "1")
    finalUri
  }

  /**
   * The client auto-closes its resources if used within a try-with-resources block. Otherwise, you should call
   * close when you're done with it to avoid resource leakage.
   */
  def close(): Unit = {
    backend.close()
  }


  /**
   * Handles the response from the server. The OpenCage server is expected to answer with a valid payload even in case of a client/server error.
   * Note that there's a difference between an HTTP request succeeding and having a success response code.
   * This method handles valid HTTP responses, and not timeouts, connection resets, etc (i.e. transport exception).
   */
  private def handleHttpRequestSuccess(httpResponse: Response[Either[io.circe.Error, OpenCageResponse]]): Try[OpenCageResponse] = {
    def handleErrorResponse(errorResponse: OpenCageResponse) = {
      httpResponse.code match {
        case StatusCodes.BadRequest => Failure(new InvalidRequestError(s"Invalid request: ${errorResponse.status.message}"))
        case StatusCodes.PaymentRequired => Failure(new QuotaExceededError(s"Quota exceeded: ${errorResponse.status.message}"))
        case StatusCodes.Forbidden => Failure(new ForbiddenError(s"Forbidden: ${errorResponse.status.message}"))
        case StatusCodes.RequestTimeout => Failure(new TimeoutError(s"Timeout: ${errorResponse.status.message}"))
        case StatusCodes.Gone => Failure(new RequestTooLongError(s"Request too long: ${errorResponse.status.message}"))
        case StatusCodes.TooManyRequests => Failure(new RateLimitExceededError(
          s"Request too long: ${errorResponse.status.message}", errorResponse.rate.map(_.reset)))
        case _ => Failure(new UnexpectedError(s"Unexpected error: ${errorResponse.status.message}"))
      }
    }

    httpResponse.body match {
      case Left(rawResp) => {
        // sttp sadly doesn't deserialize error responses, so we'll have to try it ourselves
        decode[OpenCageResponse](rawResp) match {
          case Left(err) => Failure(new DeserializationError(s"Failed to deserialize the response from the OpenCage server: $rawResp", err))
          case Right(errorResponse) => handleErrorResponse(errorResponse)
        }
      }
      case Right(eitherErrorOrResponse) => {
        // We got a 200 OK, but we need to see if we were actually able to deserialize it
        eitherErrorOrResponse match {
          case Left(err) => Failure(new DeserializationError("Failed to deserialize the response from the OpenCage server", err))
          case Right(parsedResponse) => Success(parsedResponse)
        }
      }
    }
  }

  private def buildUri(query: String, params: OpenCageClientParams): Uri = {
    // URI with default parameters
    val uri = uri"${scheme.toString}://$hostname:$port/geocode/v1/json".params(
      Map(
        "q" -> query,
        "key" -> authKey
      )
    )

    val extraParams = scala.collection.mutable.Map[String, String]()

    params.bounds.foreach { b => extraParams += ("bounds" -> b.productIterator.toList.mkString(",")) }
    params.language.foreach { l => extraParams += ("language" -> l) }
    params.minConfidence.foreach { m => extraParams += ("min_confidence" -> m.toString) }
    params.limit.foreach { l => extraParams += ("limit" -> l.toString) }
    params.proximity.foreach { p => extraParams += ("proximity" -> p.productIterator.toList.mkString(",")) }

    if (params.countryCodes.nonEmpty) extraParams += ("countrycode" -> params.countryCodes.mkString(","))

    if (params.abbreviate) extraParams += ("abbrv" -> "1")
    if (params.addRequest) extraParams += ("add_request" -> "1")
    if (params.pretty) extraParams += ("pretty" -> "1")
    if (params.withoutAnnotations) extraParams += ("no_annotations" -> "1")
    if (params.withoutDeduplication) extraParams += ("no_dedupe" -> "1")
    if (params.withoutRecord) extraParams += ("no_record" -> "1")

    uri.params(extraParams.toMap: Map[String, String])
  }
}

/**
 * Contains default configuration values that can be overridden.
 */
object OpenCageClient {
  val defaultScheme = Scheme.https
  val defaultPort = 443
  val defaultHostname = "api.opencagedata.com"
  val defaultBackend = AsyncHttpClientFutureBackend()
  val defaultUserAgent = "opencage-scala-client"

  object Scheme extends Enumeration {
    type Scheme = Value
    val http, https = Value
  }

}

/**
 * See https://geocoder.opencagedata.com/api#forward-opt.
 */
case class OpenCageClientParams(abbreviate: Boolean = false,
                                addRequest: Boolean = false,
                                bounds: Option[(Float, Float, Float, Float)] = None,
                                countryCodes: List[String] = List(),
                                language: Option[String] = None,
                                limit: Option[Int] = None,
                                minConfidence: Option[Int] = None,
                                pretty : Boolean = false,
                                proximity: Option[(Float, Float)] = None,
                                withoutAnnotations: Boolean = true,
                                withoutDeduplication: Boolean = false,
                                withoutRecord: Boolean = false)
