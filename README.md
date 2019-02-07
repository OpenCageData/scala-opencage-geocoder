# scala-opencage-geocoder

[![Build Status](https://travis-ci.org/OpenCageData/scala-opencage-geocoder.svg?branch=master)](https://travis-ci.org/OpenCageData/scala-opencage-geocoder)

This is a client library for the [OpenCage Forward and Reverse geocoding APIs](https://opencagedata.com/api).
You will need to have an API key to be able to issue requests. You can register for free [here](https://opencagedata.com/users/sign_up).

Before using this library please have a look at [best practices for using the OpenCage API](https://opencagedata.com/api#bestpractices), 
particularly OpenCage's advice for [how to format forward geocoding queries](https://github.com/OpenCageData/opencagedata-misc-docs/blob/master/query-formatting.md).

## Building

You'll need to install [sbt](https://www.scala-sbt.org/) first. Then you can just run:

```
    sbt clean test package
```

## Forward geocoding

To perform forward geocoding, all you need to do is call the `forwardGeocode` method, as is shown before:

```scala
    val client = new OpenCageClient(authKey)
    try {
        val responseFuture = client.forwardGeocode("address to forward geocode")
        val response = Await.result(responseFuture, 5 seconds)
    }
    finally {
        client.close()
    }
```

Note that the client is non-blocking, so if you're handling responses in an async way, you won't need to await for the future. 

## Reverse geocoding

To perform reverse geocoding, all you need to do is call the `reverseGeocode` method with the latitude/longitude you want to reverse geocode, as is shown before:

```scala
    val client = new OpenCageClient(authKey)
    try {
        val latitude = 52.51627f
        val longitue = 3.37769f
        val responseFuture = client.reverseGeocode(latitude, longitude)
        val response = Await.result(responseFuture, 5 seconds)
    }
    finally {
        client.close()
    }
```

## No results

Sometimes you query the OpenCage API and it cannot geocode your query, it was a valid query, but no results were found.
You can check if that's the case by checking status code of the response and emptiness of the results.

```scala
val response = Await.result(responseFuture, 5.seconds)
if (response.status.code == 200 & response.results.isEmpty) {
  println("Ups, we can't geolocate your query")
}
```

Note that the client is non-blocking, so if you're handling responses in an async way, you won't need to await for the future. 

## Closing the client

To make sure you don't leave resources dangling around, make sure you call the `close()` method on the client when you don't need it anymore, so any connections still open can be closed.

## Parameters

The parameters sent by the client to the OpenCage APIs can be overridden (see [this](https://opencagedata.com/api#forward-opt)), by using the `params` parameter:
 
```scala
    val bounds = Some((minimumLongitude, minimumLatitude, maximumLongitude, maximumLatitude))
    
    val params = OpenCageClientParams(abbreviate = true,
                                     bounds = bounds,
                                     countryCodes = List("fr,bl,gf,gp,mf,mq,nc,pf,pm,re,tf,wf,yt"),
                                     language = "fr",
                                     limit = 10,
                                     minConfidence = 5,
                                     withoutAnnotations = false,
                                     withoutDeduplication = true,
                                     withoutRecord = true)
                                     
    client.reverseGeocode(latitude, longitude, params)
    
```

This is particularly useful for forward geocoding to help improve your results.

## Sample application

Below you have a code snippet of a minimal application using OpenCage client. First create a minimal project scaffolding
by issuing `sbt new scala/scala-seed.g8`. Then place in `src/main/scala/example/` this file

```scala
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
        val response = Await.result(responseFuture, 5.seconds)

        println(response)

        client.close()
      case None => System.exit(1)
    }
  }
}

case class Config(query: String = "", key: String = "")
```

and don't forget to add `Scopt` and `OpenCage` dependencies with desired versions to the newly created `build.sbt`

```scala
libraryDependencies ++= Seq(
    "com.github.scopt" %% "scopt" % "X.Y.Z",
    "com.opengage" %% "scala-opencage-geocoder" % "X.Y.Z"
)
```

Then if you'd like to try the sample application included with this library, just run (e.g. forward geocoding the Brandenburg Gate):

```
    sbt 'run -q "Brandenburg Gate" -k <your key>'
```

## Client configuration

The client can be configured by passing one or more of the optional constructor arguments:

```scala
    val client = new OpenCageClient(authKey: String,
                                    scheme: Scheme = OpenCageClient.defaultScheme,
                                    hostname: String = OpenCageClient.defaultHostname,
                                    port: Int = OpenCageClient.defaultPort,
                                    executionContext: ExecutionContext = ExecutionContext.global,
                                    backend: SttpBackend[Future, Nothing] = OpenCageClient.defaultBackend)
```

* `scheme` - Allows you to specify if the request is to be made over HTTP or HTTPS (for testing purposes, if you're mocking the API)
* `hostname` - Allows you to specify the hostname (for testing purposes, if you're mocking the API)
* `port` - Allows you to specify the port (for testing purposes, if you're mocking the API)
* `executionContext` - Allows you to specify the thread pool to be used for processing the responses
* `backend` - Allows you to specify a different backend, or to customise the default one.

### Customising the backend

The client uses the [async-http-client](https://github.com/AsyncHttpClient/async-http-client) backend from [sttp](http://sttp.readthedocs.io/en/latest/backends/asynchttpclient.html) by default.

However, you can configure `async-http-client` by using your own config object as per below (e.g. setting maximum number of connections in the pool):
 
```scala
    val config = new DefaultAsyncHttpClientConfig.Builder().setMaxConnections(10).build()
    val backend = AsyncHttpClientFutureBackend.withConfig(config)
```

Or by specifying options, such as a connection timeout or proxy by using custom backend options directly via `sttp`:
```scala
    val options = SttpBackendOptions(connectionTimeout, proxy)
    val backend = AsyncHttpClientFutureBackend(options)
```

Finally, you can also implement your own `sttp` backend, as long as it's asynchronous.

### Author

This code was originally written by [Nuno Guerreiro](https://github.com/nmdguerreiro/) who later transfered it to the OpenCage organisation. Thank you Nuno!

License
-------

[MIT License](LICENSE.md).
