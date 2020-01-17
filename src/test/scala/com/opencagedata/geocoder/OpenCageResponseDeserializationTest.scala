package com.opencagedata.geocoder

import java.time.Instant

import com.opencagedata.geocoder.parts._
import io.circe.parser._
import org.scalatest.{ FlatSpec, Matchers }

/**
 * Validates that all attributes are deserialized correctly.
 */
class OpenCageResponseDeserializationTest extends FlatSpec with Matchers {
  "Deserialization" should "handle reverse geocoding responses correctly" in {
    val decodedResponse = decode[OpenCageResponse](DeserializationsResponseData.validReverseResponseRaw)

    assert(decodedResponse.isRight)
    assertResult(DeserializationsResponseData.validReverseResponseMapped)(decodedResponse.right.get)
  }

  it should "handle forward geocoding responses correctly" in {
    val decodedResponse = decode[OpenCageResponse](DeserializationsResponseData.validForwardResponse)

    assert(decodedResponse.isRight)
    assertResult(DeserializationsResponseData.validForwardResponseMapped)(decodedResponse.right.get)
  }

  it should "handle annotations correctly" in {
    val decodedResponse = decode[OpenCageResponse](DeserializationsResponseData.validReverseResponseWithAnnotations)

    assert(decodedResponse.isRight)
    assert(decodedResponse.right.get.results.size == 1)
    assertResult(DeserializationsResponseData.annotationsMapped)(decodedResponse.right.get.results(0).annotations.get)
  }

  it should "handle annotations with string offset with leading zero correctly" in {
    val decodedResponse = decode[OpenCageResponse](DeserializationsResponseData.validReverseResponseWithAnnotationsAndStringOffset)

    assert(decodedResponse.isRight)
    assert(decodedResponse.right.get.results.size == 1)
    assertResult(DeserializationsResponseData.annotationsWithStringOffsetMapped)(decodedResponse.right.get.results(0).annotations.get)
  }

  it should "handle errors correctly" in {
    val decodedResponse = decode[OpenCageResponse](DeserializationsResponseData.validError)

    assertResult(DeserializationsResponseData.validErrorMapped)(decodedResponse.right.get)
  }
}

object DeserializationsResponseData {
  val now = Instant.now().toEpochMilli / 1000
  val nowFormatted = Instant.now().toString
  val tomorrow = Instant.now().toEpochMilli / 1000 + 86400

  /**
   * Valid response data without annotations
   */
  val validReverseResponseRaw =
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

  private val brandenburgGateComponents: Some[Components] = Some(
    Components(
      iso3166Alpha2Code = Some("DE"),
      componentType = "attraction",
      attraction = Some("Brandenburg Gate"),
      city = Some("Berlin"),
      cityDistrict = Some("Mitte"),
      country = Some("Germany"),
      countryCode = Some("de"),
      houseNumber = Some("1"),
      politicalUnion = Some("European Union"),
      postcode = Some("10117"),
      road = Some("Pariser Platz"),
      state = Some("Berlin"),
      suburb = Some("Mitte"),
      village = None
    )
  )

  val validReverseResponseMapped = new OpenCageResponse(
    documentation = "https://geocoder.opencagedata.com/api",
    licenses = List(License("CC-BY-SA", "http://creativecommons.org/licenses/by-sa/3.0/")),
    rate = Some(RateLimits(2500, 2499, Instant.ofEpochSecond(DeserializationsResponseData.tomorrow))),
    results = List(
      Result(
        annotations = None,
        bounds = Some(Bounds(LatLong(52.5164327f, 13.377825f), LatLong(52.5161167f, 13.37758f))),
        components = brandenburgGateComponents,
        formattedAddress = Some("Brandenburg Gate, Pariser Platz 1, 10117 Berlin, Germany"),
        confidence = Some(9),
        geometry = Some(LatLong(52.5162767f, 13.3777025f))
      )
    ),
    status = Status(200, "OK"),
    timestamp = ServerTimestamp(DeserializationsResponseData.nowFormatted, Instant.ofEpochSecond(DeserializationsResponseData.now)),
    totalResults = 1
  )

  /**
   * Valid response data without annotations
   */
  val validReverseResponseWithAnnotations =
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

  val annotationsMapped = Annotations(
    dms = Some(DMS("52\u00b0 30' 58.59612'' N", "13\u00b0 22' 39.72900'' E")),
    mgrs = Some("33UUU8991719699"),
    maidenhead = Some("JO62qm53hv"),
    mercator = Some(MercatorProjection(1489199.031f, 6860089.217f)),
    osm = Some(OSM(
      "https://www.openstreetmap.org/edit?way=518071791#map=17/52.51628/13.37770",
      "https://www.openstreetmap.org/?mlat=52.51628&mlon=13.37770#map=17/52.51628/13.37770"
    )),
    currency = Some(Currency(
      alternateSymbols = Some(List()),
      decimalMark = Some(","),
      htmlEntity = Some("&#x20AC;"),
      isoCode = Some("EUR"),
      isoNumeric = Some(978),
      name = Some("Euro"),
      smallestDenomination = Some(1),
      subunit = Some("Cent"),
      subunitToUnit = Some(100),
      symbol = Some("\u20ac"),
      symbolFirst = Some(1),
      thousandsSeparator = Some(".")
    )),
    flag = Some("\ud83c\udde9\ud83c\uddea"),
    callingCode = Some(49),
    geohash = Some("u33db2m37p9bznzem3pq"),
    qibla = Some(136.64f),
    sun = Some(Sun(
      rise = SunTimings(Instant.ofEpochSecond(now), Instant.ofEpochSecond(now), Instant.ofEpochSecond(now), Instant.ofEpochSecond(now)),
      set = SunTimings(Instant.ofEpochSecond(now), Instant.ofEpochSecond(now), Instant.ofEpochSecond(now), Instant.ofEpochSecond(now))
    )),
    timezone = Some(Timezone("Europe/Berlin", false, 3600, 100, "CET")),
    what3Words = Some(What3Words("glosses.hood.bags")),
    wikidata = Some("Q82425")
  )

  /**
   * Valid response for forward geocoding without annotations
   */
  val validForwardResponse =
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

  val validForwardResponseMapped = new OpenCageResponse(
    documentation = "https://geocoder.opencagedata.com/api",
    licenses = List(License("CC-BY-SA", "http://creativecommons.org/licenses/by-sa/3.0/")),
    rate = Some(RateLimits(2500, 2499, Instant.ofEpochSecond(DeserializationsResponseData.tomorrow))),
    results = List(
      Result(
        annotations = None,
        bounds = Some(Bounds(LatLong(52.5164327f, 13.377825f), LatLong(52.5161167f, 13.37758f))),
        components = brandenburgGateComponents,
        formattedAddress = Some("Brandenburg Gate, Pariser Platz 1, 10117 Berlin, Germany"),
        confidence = Some(9),
        geometry = Some(LatLong(52.5162767f, 13.3777025f))
      )
    ),
    status = Status(200, "OK"),
    timestamp = ServerTimestamp(DeserializationsResponseData.nowFormatted, Instant.ofEpochSecond(DeserializationsResponseData.now)),
    totalResults = 1
  )

  /**
   * Valid response data without annotations with string offset
   *
   * Note the offset_string value has a leading 0 and is wrapped as a string
   */
  val validReverseResponseWithAnnotationsAndStringOffset =
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
	         |                "name":"America/New_York",
	         |                "now_in_dst":0,
	         |                "offset_sec":-18000,
	         |                "offset_string":"-0500",
	         |                "short_name":"EST"
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

  val annotationsWithStringOffsetMapped = Annotations(
    dms = Some(DMS("52\u00b0 30' 58.59612'' N", "13\u00b0 22' 39.72900'' E")),
    mgrs = Some("33UUU8991719699"),
    maidenhead = Some("JO62qm53hv"),
    mercator = Some(MercatorProjection(1489199.031f, 6860089.217f)),
    osm = Some(
      OSM(
        "https://www.openstreetmap.org/edit?way=518071791#map=17/52.51628/13.37770",
        "https://www.openstreetmap.org/?mlat=52.51628&mlon=13.37770#map=17/52.51628/13.37770"
      )
    ),
    currency = Some(
      Currency(
        alternateSymbols = Some(List()),
        decimalMark = Some(","),
        htmlEntity = Some("&#x20AC;"),
        isoCode = Some("EUR"),
        isoNumeric = Some(978),
        name = Some("Euro"),
        smallestDenomination = Some(1),
        subunit = Some("Cent"),
        subunitToUnit = Some(100),
        symbol = Some("\u20ac"),
        symbolFirst = Some(1),
        thousandsSeparator = Some(".")
      )
    ),
    flag = Some("\ud83c\udde9\ud83c\uddea"),
    callingCode = Some(49),
    geohash = Some("u33db2m37p9bznzem3pq"),
    qibla = Some(136.64f),
    sun = Some(
      Sun(
        rise = SunTimings(
          Instant.ofEpochSecond(now),
          Instant.ofEpochSecond(now),
          Instant.ofEpochSecond(now),
          Instant.ofEpochSecond(now)
        ),
        set = SunTimings(
          Instant.ofEpochSecond(now),
          Instant.ofEpochSecond(now),
          Instant.ofEpochSecond(now),
          Instant.ofEpochSecond(now)
        )
      )
    ),
    timezone = Some(Timezone("America/New_York", false, -18000, -500, "EST")),
    what3Words = Some(What3Words("glosses.hood.bags")),
    wikidata = Some("Q82425")
  )

  /**
   * Valid errors
   */
  val validError =
    s"""
       |{
       |   "documentation" : "https://geocoder.opencagedata.com/api",
       |   "licenses" : [
       |      {
       |         "name" : "CC-BY-SA",
       |         "url" : "http://creativecommons.org/licenses/by-sa/3.0/"
       |      }
       |   ],
       |   "results" : [],
       |   "status" : {
       |      "code" : 403,
       |      "message" : "Denied!"
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

  val validErrorMapped = new OpenCageResponse(
    documentation = "https://geocoder.opencagedata.com/api",
    licenses = List(License("CC-BY-SA", "http://creativecommons.org/licenses/by-sa/3.0/")),
    results = List(),
    rate = None,
    status = Status(403, "Denied!"),
    timestamp = ServerTimestamp(DeserializationsResponseData.nowFormatted, Instant.ofEpochSecond(DeserializationsResponseData.now)),
    totalResults = 0
  )
}