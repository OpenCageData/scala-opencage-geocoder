# scala-opencage-geocoder

[![Build Status](https://travis-ci.org/nmdguerreiro/scala-opencage-geocoder.svg?branch=master)](https://travis-ci.org/nmdguerreiro/scala-opencage-geocoder)

This is a client library for the [OpenCage Forward and Reverse geocoding APIs](https://geocoder.opencagedata.com/api).
You will need to have an API key to be able to issue requests. You can register for free [here](https://geocoder.opencagedata.com/users/sign_up).

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

Note that the client is non-blocking, so if you're handling responses in an async way, you won't need to await for the future. 

## Closing the client

To make sure you don't leave resources dangling around, make sure you call the `close()` method on the client when you don't need it anymore, so any connections still open can be closed.

## Parameters

The parameters sent by the client to the OpenCage APIs can be overridden (see [this](https://geocoder.opencagedata.com/api#forward-opt)), by using the `params` parameter:
 
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

If you'd like to try the sample application included with this library, just run (e.g. forward geocoding the Brandenburg Gate):

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
