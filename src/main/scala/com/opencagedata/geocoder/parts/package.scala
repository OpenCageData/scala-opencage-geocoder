package com.opencagedata.geocoder

import java.time.Instant

import io.circe.generic.extras.{ Configuration, ConfiguredJsonCodec }
import io.circe.{ Decoder, Encoder }

package object parts {

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val decodeInstant: Decoder[Instant] = Decoder.decodeLong.map { l: Long => Instant.ofEpochSecond(l) }
  implicit val encodeInstant: Encoder[Instant] = Encoder.encodeLong.contramap[Instant](_.getEpochSecond)

  implicit val decodeBoolean: Decoder[Boolean] = Decoder.decodeInt.map { i: Int => i != 0 }
  implicit val encodeBoolean: Encoder[Boolean] = Encoder.encodeInt.contramap[Boolean]({ b: Boolean => if (b) 1 else 0 })

  @ConfiguredJsonCodec case class LatLong(lat: Float, lng: Float)

  /**
   * See https://geocoder.opencagedata.com/api#rate-limiting
   * @param limit the total number of transactions that your account is limited to over a 24 hour period
   * @param remaining the number of transactions remaining in the current 24 hour period
   * @param reset the instant at which your transaction count will reset
   */
  @ConfiguredJsonCodec case class RateLimits(limit: Int, remaining: Int, reset: Instant)

  @ConfiguredJsonCodec case class License(name: String, url: String)

  @ConfiguredJsonCodec case class Status(code: Int, message: String)

  @ConfiguredJsonCodec case class Bounds(northeast: LatLong, southwest: LatLong)

  @ConfiguredJsonCodec case class Components(
    iso3166Alpha2Code: Option[String],
    componentType:     String,
    attraction:        Option[String],
    city:              Option[String],
    cityDistrict:      Option[String],
    houseNumber:       Option[String],
    postcode:          Option[String],
    country:           Option[String],
    countryCode:       Option[String],
    politicalUnion:    Option[String],
    road:              Option[String],
    state:             Option[String],
    suburb:            Option[String],
    village:           Option[String]
  )

  object Components {
    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames.copy(
      transformMemberNames = {
        case "iso3166Alpha2Code" => "ISO_3166-1_alpha-2"
        case "componentType"     => "_type"
        case other               => Configuration.default.withSnakeCaseMemberNames.transformMemberNames(other)
      }
    )
  }

  /**
   * See https://geocoder.opencagedata.com/api#annotations
   */

  @ConfiguredJsonCodec case class DMS(lat: String, lng: String)

  @ConfiguredJsonCodec case class MercatorProjection(x: Float, y: Float)

  @ConfiguredJsonCodec case class OSM(editUrl: String, url: String)

  @ConfiguredJsonCodec case class What3Words(words: String)

  @ConfiguredJsonCodec case class Currency(
    alternateSymbols:     Option[List[String]],
    decimalMark:          Option[String],
    htmlEntity:           Option[String],
    isoCode:              Option[String],
    isoNumeric:           Option[Int],
    name:                 Option[String],
    smallestDenomination: Option[Int],
    subunit:              Option[String],
    subunitToUnit:        Option[Int],
    symbol:               Option[String],
    symbolFirst:          Option[Int],
    thousandsSeparator:   Option[String]
  )

  @ConfiguredJsonCodec case class SunTimings(
    apparent:     Instant,
    astronomical: Instant,
    civil:        Instant,
    nautical:     Instant
  )

  @ConfiguredJsonCodec case class Sun(rise: SunTimings, set: SunTimings)

  @ConfiguredJsonCodec case class Timezone(
    name:         String,
    nowInDst:     Boolean,
    offsetSec:    Int,
    offsetString: Int,
    shortName:    String
  )

  @ConfiguredJsonCodec case class Annotations(
    currency:    Option[Currency],
    dms:         Option[DMS],
    mgrs:        Option[String],
    osm:         Option[OSM],
    maidenhead:  Option[String],
    flag:        Option[String],
    mercator:    Option[MercatorProjection],
    callingCode: Option[Int],
    geohash:     Option[String],
    qibla:       Option[Float],
    wikidata:    Option[String],
    what3Words:  Option[What3Words],
    sun:         Option[Sun],
    timezone:    Option[Timezone]
  )

  object Annotations {
    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames.copy(
      transformMemberNames = {
        case "dms"         => "DMS"
        case "mgrs"        => "MGRS"
        case "osm"         => "OSM"
        case "maidenhead"  => "Maidenhead"
        case "mercator"    => "Mercator"
        case "callingCode" => "callingcode"
        case "what3Words"  => "what3words"
        case other         => other
      }
    )
  }

  @ConfiguredJsonCodec case class Result(
    annotations:      Option[Annotations],
    bounds:           Option[Bounds],
    components:       Option[Components],
    confidence:       Option[Int],
    formattedAddress: Option[String],
    geometry:         Option[LatLong]
  )

  object Result {
    implicit val config: Configuration = Configuration.default.copy(
      transformMemberNames = {
        case "formattedAddress" => "formatted"
        case other              => other
      }
    )
  }

  @ConfiguredJsonCodec case class ServerTimestamp(formatted: String, time: Instant)
  object ServerTimestamp {
    implicit val config: Configuration = Configuration.default.copy(
      transformMemberNames = {
        case "formatted" => "created_http"
        case "time"      => "created_unix"
        case other       => other
      }
    )

    implicit val decodeInstant: Decoder[Instant] = Decoder.decodeLong.map { l: Long => Instant.ofEpochSecond(l) }
  }

}
