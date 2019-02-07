package com.opencage.geocoder

import com.opencage.geocoder.parts._
import io.circe.generic.extras.ConfiguredJsonCodec

/**
 * See https://geocoder.opencagedata.com/api#response for more details on the response format
 */
@ConfiguredJsonCodec case class OpenCageResponse(documentation: String,
                                                 licenses: List[License],
                                                 rate: Option[RateLimits],
                                                 results: List[Result],
                                                 status: Status,
                                                 timestamp: ServerTimestamp,
                                                 totalResults: Long)
