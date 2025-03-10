package com.example.weatherservice.load

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class WeatherServiceLoadTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .header("Accept", "application/json")
    .header("Content-Type", "application/json")

  val locations = Array(
    "New York",
    "London",
    "Paris",
    "Tokyo",
    "Sydney",
    "Berlin",
    "Rome",
    "Madrid",
    "Moscow",
    "Beijing"
  )

  val currentWeatherScenario = scenario("Current Weather API Load Test")
    .repeat(10) {
      exec(
        http("Current Weather Request")
          .get("/api/weather/current")
          .queryParam("location", session => locations(scala.util.Random.nextInt(locations.length)))
          .check(status.is(200))
          .check(jsonPath("$.location").exists)
          .check(jsonPath("$.temperature").exists)
      )
      .pause(1.second)
    }

  val forecastScenario = scenario("Forecast API Load Test")
    .repeat(10) {
      exec(
        http("Forecast Request")
          .get("/api/weather/forecast")
          .queryParam("location", session => locations(scala.util.Random.nextInt(locations.length)))
          .queryParam("days", 5)
          .check(status.is(200))
          .check(jsonPath("$.location").exists)
          .check(jsonPath("$.dailyForecasts").exists)
      )
      .pause(1.second)
    }

  val reportScenario = scenario("Weather Report API Load Test")
    .repeat(10) {
      exec(
        http("Weather Report Request")
          .get("/api/weather/report")
          .queryParam("location", session => locations(scala.util.Random.nextInt(locations.length)))
          .queryParam("days", 5)
          .check(status.is(200))
          .check(jsonPath("$.currentWeather").exists)
          .check(jsonPath("$.forecast").exists)
      )
      .pause(1.second)
    }

  setUp(
    currentWeatherScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(10).during(1.minute)
    ),
    forecastScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(10).during(1.minute)
    ),
    reportScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(10).during(1.minute)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(5000),
     global.successfulRequests.percent.gt(95)
   )
}