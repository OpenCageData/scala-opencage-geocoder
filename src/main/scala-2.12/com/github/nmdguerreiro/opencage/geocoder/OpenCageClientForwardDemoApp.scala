package com.github.nmdguerreiro.opencage.geocoder

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * A simple sample app to show how to use the client.
 */
object OpenCageClientForwardDemoApp {
  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("OpenCageClientApp") {
      head("OpenCageClientApp", "0.1")

      opt[String]('q', "query").required().action( (q, c) =>
        c.copy(query = q) ).text("the query you want issue")

      opt[String]('k', "key").required().action( (k, c) =>
        c.copy(key = k) ).text("your authentication key")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>

        val client = new OpenCageClient(config.key)
        val responseFuture = client.forwardGeocode(config.query)
        val response = Await.result(responseFuture, 5 seconds)

        println(response)

        client.close()
      case None => System.exit(1)
    }
  }
}

case class Config(query: String = "", key: String = "")