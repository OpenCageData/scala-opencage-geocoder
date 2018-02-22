package com.github.nmdguerreiro.opencage.geocoder

import java.time.Instant
import java.util.UUID

import com.github.nmdguerreiro.opencage.geocoder.parts._
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfter, AsyncFlatSpec, BeforeAndAfterEach, Matchers}

import scala.reflect.ClassTag

class OpencageClientTest extends AsyncFlatSpec with Matchers with BeforeAndAfterEach {

  val wireMockServer = new WireMockServer(wireMockConfig().dynamicPort())
  val host = "localhost"

  val requestUrl = "/geocode/v1/json"
  val validCoords = (52.51627f, 13.37769f)
  val validKey = "1234"
  val invalidKey = "2345"

  override def beforeEach {
    wireMockServer.start()
    WireMock.configureFor(host, wireMockServer.port())
  }

  override def afterEach {
    wireMockServer.resetAll()
    wireMockServer.stop()
  }

  "Reverse geocoder" should "make a call with the default parameters" in {
    val reverseQuery = s"${validCoords._1},${validCoords._2}"

    val client = new OpenCageClient(validKey,
      hostname = host,
      scheme = OpenCageClient.Scheme.http,
      port = wireMockServer.port())

    stubFor(get(urlPathEqualTo(requestUrl))
      .willReturn(
        aResponse()
          .withStatus(200)
          .withBody(ResponseData.validResponseString)))

    val respFuture = client.reverseGeocode(validCoords._1, validCoords._2)

    respFuture map {
      resp: OpenCageResponse => {
        verify(getRequestedFor(urlPathEqualTo(requestUrl))
          .withQueryParam("q", equalTo(reverseQuery))
          .withQueryParam("key", equalTo(validKey))
          .withQueryParam("no_annotations", equalTo("1")))

        assert(wireMockServer.findAllUnmatchedRequests().size == 0)
      }
    }
  }

  "Forward geocoder" should "make a call with the default parameters" in {
    val forwardQuery = "Branderburg Gate"

    val client = new OpenCageClient(validKey,
      hostname = host,
      scheme = OpenCageClient.Scheme.http,
      port = wireMockServer.port())

    stubFor(get(urlPathEqualTo(requestUrl))
      .willReturn(
        aResponse()
          .withStatus(200)
          .withBody(ResponseData.validResponseString)))

    val respFuture = client.forwardGeocode(forwardQuery)

    respFuture map {
      resp: OpenCageResponse => {
        verify(getRequestedFor(urlPathEqualTo(requestUrl))
          .withQueryParam("q", equalTo(s"$forwardQuery"))
          .withQueryParam("key", equalTo(validKey))
          .withQueryParam("no_annotations", equalTo("1")))

        assert(wireMockServer.findAllUnmatchedRequests().size == 0)
      }
    }
  }

  it should "make a call with all the custom parameters" in {
    val forwardQuery = "Branderburg Gate"

    val abbreviate = true
    val bounds = (1f, 2f, 3f, 4f)
    val countryCodes = List("gb", "de")
    val language = "en"
    val limit = 100
    val minConfidence = 10
    val withoutAnnotations = false
    val withoutDeduplication = true
    val withoutRecord = true

    val params = OpenCageClientParams(
      abbreviate = abbreviate,
      bounds = Some(bounds),
      countryCodes = countryCodes,
      language = Some(language),
      limit = Some(limit),
      minConfidence = Some(minConfidence),
      withoutAnnotations = withoutAnnotations,
      withoutDeduplication = withoutDeduplication,
      withoutRecord = withoutRecord
    )

    val client = new OpenCageClient(validKey,
      hostname = host,
      scheme = OpenCageClient.Scheme.http,
      port = wireMockServer.port())

    stubFor(get(urlPathEqualTo(requestUrl))
      .willReturn(
        aResponse()
          .withStatus(200)
          .withBody(ResponseData.validResponseString)))

    val respFuture = client.forwardGeocode(forwardQuery, params)

    respFuture map {
      resp: OpenCageResponse => {
        verify(getRequestedFor(urlPathEqualTo(requestUrl))
          .withQueryParam("q", equalTo(forwardQuery))
          .withQueryParam("key", equalTo(validKey))
          .withQueryParam("abbrv", equalTo("1"))
          .withQueryParam("bounds", equalTo(bounds.productIterator.toList.mkString(",")))
          .withQueryParam("countrycode", equalTo(countryCodes.mkString(",")))
          .withQueryParam("language", equalTo(language))
          .withQueryParam("limit", equalTo(limit.toString))
          .withQueryParam("min_confidence", equalTo(minConfidence.toString))
          .withQueryParam("no_dedupe", equalTo("1"))
          .withQueryParam("no_record", equalTo("1")))

        assert(wireMockServer.findAllUnmatchedRequests().size == 0)
      }
    }
  }

  "Error handling" should "support invalid key errors" in {
    val client = new OpenCageClient(invalidKey,
      hostname = host,
      scheme = OpenCageClient.Scheme.http,
      port = wireMockServer.port())

    val requestUrl = s"/geocode/v1/json?q=${validCoords._1},${validCoords._2}&key=$invalidKey&no_annotations=1"

    stubFor(get(urlEqualTo(requestUrl))
      .willReturn(
        aResponse()
          .withStatus(403)
          .withBody(ResponseData.invalidKeyError)))

    recoverToSucceededIf[ForbiddenError] {
      client.reverseGeocode(validCoords._1, validCoords._2)
    }
  }

  it should "support invalid request errors" in {
    genericError[InvalidRequestError]("badrequest", 400, ResponseData.invalidRequestError)
  }

  it should "support quota exceeded errors" in {
    genericError[QuotaExceededError]("quotaexceeded", 402, ResponseData.quotaExceededError)
  }

  it should "support rate limit exceeded errors" in {
    genericError[RateLimitExceededError]("ratelimit", 429, ResponseData.rateLimitExceededError)
  }

  it should "support timeout errors" in {
    genericError[TimeoutError]("timeout", 408, ResponseData.timeoutError)
  }

  it should "support request too long errors" in {
    genericError[RequestTooLongError]("timeout", 410, ResponseData.requestTooLongError)
  }

  it should "support connection errors" in {
    val client = new OpenCageClient(validKey,
      hostname = "some-random-host" + UUID.randomUUID().toString,
      scheme = OpenCageClient.Scheme.http,
      port = wireMockServer.port())

    recoverToSucceededIf[OpencageClientError] {
      client.forwardGeocode("someinvaliddata")
    }
  }

  private def genericError[T <: OpencageClientError](errorPath: String, errorCode: Int, responseBody: String)
                                                    (implicit classTag: ClassTag[T]) = {
    val client = new OpenCageClient(validKey,
      hostname = host,
      scheme = OpenCageClient.Scheme.http,
      port = wireMockServer.port())

    val requestUrl = s"/geocode/v1/json?q=$errorPath&key=$validKey&no_annotations=1"

    stubFor(get(urlEqualTo(requestUrl))
      .willReturn(
        aResponse()
          .withStatus(errorCode)
          .withBody(responseBody)))

    recoverToSucceededIf[T] {
      client.forwardGeocode(errorPath)
    }
  }
}


object ResponseData {
  val now = Instant.now().toEpochMilli / 1000
  val nowFormatted = Instant.now().toString
  val tomorrow = Instant.now().toEpochMilli / 1000 + 86400

  /**
   * Valid response data
   */

  val validResponseString =
    s"""
       |{
       |   "documentation" : "https://geocoder.opencagedata.com/api",
       |   "licenses" : [
       |      {
       |         "name" : "CC-BY-SA",
       |         "url" : "http://creativecommons.org/licenses/by-sa/3.0/"
       |      }
       |   ],
       |   "rate" : {
       |      "limit" : 2500,
       |      "remaining" : 2499,
       |      "reset" : $tomorrow
       |   },
       |   "results" : [
       |      {
       |         "annotations" : {
       |            "DMS" : {
       |               "lat" : "52\u00b0 30' 58.59612'' N",
       |               "lng" : "13\u00b0 22' 39.72900'' E"
       |            },
       |            "MGRS" : "33UUU8991719699",
       |            "Maidenhead" : "JO62qm53hv",
       |            "Mercator" : {
       |               "x" : 1489199.031,
       |               "y" : 6860089.217
       |            },
       |            "OSM" : {
       |               "edit_url" : "https://www.openstreetmap.org/edit?way=518071791#map=17/52.51628/13.37770",
       |               "url" : "https://www.openstreetmap.org/?mlat=52.51628&mlon=13.37770#map=17/52.51628/13.37770"
       |            },
       |            "callingcode" : 49,
       |            "currency" : {
       |               "alternate_symbols" : [],
       |               "decimal_mark" : ",",
       |               "html_entity" : "&#x20AC;",
       |               "iso_code" : "EUR",
       |               "iso_numeric" : 978,
       |               "name" : "Euro",
       |               "smallest_denomination" : 1,
       |               "subunit" : "Cent",
       |               "subunit_to_unit" : 100,
       |               "symbol" : "\u20ac",
       |               "symbol_first" : 1,
       |               "thousands_separator" : "."
       |            },
       |            "flag" : "\ud83c\udde9\ud83c\uddea",
       |            "geohash" : "u33db2m37p9bznzem3pq",
       |            "qibla" : 136.64,
       |            "sun" : {
       |               "rise" : {
       |                  "apparent" : $now,
       |                  "astronomical" : $now,
       |                  "civil" : $now,
       |                  "nautical" : $now
       |               },
       |               "set" : {
       |                  "apparent" : $now,
       |                  "astronomical" : $now,
       |                  "civil" : $now,
       |                  "nautical" : $now
       |               }
       |            },
       |            "timezone" : {
       |               "name" : "Europe/Berlin",
       |               "now_in_dst" : 0,
       |               "offset_sec" : 3600,
       |               "offset_string" : 100,
       |               "short_name" : "CET"
       |            },
       |            "what3words" : {
       |               "words" : "glosses.hood.bags"
       |            },
       |            "wikidata" : "Q82425"
       |         },
       |         "bounds" : {
       |            "northeast" : {
       |               "lat" : 52.5164327,
       |               "lng" : 13.377825
       |            },
       |            "southwest" : {
       |               "lat" : 52.5161167,
       |               "lng" : 13.37758
       |            }
       |         },
       |         "components" : {
       |            "ISO_3166-1_alpha-2" : "DE",
       |            "_type" : "attraction",
       |            "attraction" : "Brandenburg Gate",
       |            "city" : "Berlin",
       |            "city_district" : "Mitte",
       |            "country" : "Germany",
       |            "country_code" : "de",
       |            "house_number" : "1",
       |            "political_union" : "European Union",
       |            "postcode" : "10117",
       |            "road" : "Pariser Platz",
       |            "state" : "Berlin",
       |            "suburb" : "Mitte"
       |         },
       |         "confidence" : 9,
       |         "formatted" : "Brandenburg Gate, Pariser Platz 1, 10117 Berlin, Germany",
       |         "geometry" : {
       |            "lat" : 52.5162767,
       |            "lng" : 13.3777025
       |         }
       |      }
       |   ],
       |   "status" : {
       |      "code" : 200,
       |      "message" : "OK"
       |   },
       |   "stay_informed" : {
       |      "blog" : "https://blog.opencagedata.com",
       |      "twitter" : "https://twitter.com/opencagedata"
       |   },
       |   "thanks" : "For using an OpenCage Data API",
       |   "timestamp" : {
       |      "created_http" : "$nowFormatted",
       |      "created_unix" : $now
       |   },
       |   "total_results" : 1
       |}
    """.stripMargin

  /**
   * Errors
   */
  val invalidRequestError = genericErrorMessage(400, "Bad, bad request!")
  val quotaExceededError = genericErrorMessage(402, "need more money")
  val invalidKeyError = genericErrorMessage(403, "You shall not pass!")
  val timeoutError = genericErrorMessage(408, "I'm all out of time")
  val requestTooLongError = genericErrorMessage(410, "try typing less")
  val rateLimitExceededError = genericErrorMessage(429, "hold your fire")

  private def genericErrorMessage(code: Int, message: String) = {
    s"""
       |{
       |   "documentation" : "https://geocoder.opencagedata.com/api",
       |   "licenses" : [
       |      {
       |         "name" : "CC-BY-SA",
       |         "url" : "http://creativecommons.org/licenses/by-sa/3.0/"
       |      },
       |      {
       |         "name" : "ODbL",
       |         "url" : "http://opendatacommons.org/licenses/odbl/summary/"
       |      }
       |   ],
       |   "results" : [],
       |   "status" : {
       |      "code" : $code,
       |      "message" : "$message"
       |   },
       |   "stay_informed" : {
       |      "blog" : "https://blog.opencagedata.com",
       |      "twitter" : "https://twitter.com/opencagedata"
       |   },
       |   "thanks" : "For using an OpenCage Data API",
       |   "timestamp" : {
       |      "created_http" : "$nowFormatted",
       |      "created_unix" : $now
       |   },
       |   "total_results" : 0
       |}
    """.stripMargin
  }

}


