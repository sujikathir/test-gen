package com.example.weatherservice.cucumber;

import com.example.weatherservice.model.WeatherData;
import com.example.weatherservice.model.WeatherForecast;
import com.example.weatherservice.service.WeatherAggregatorService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WeatherStepDefinitions {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;
    private WeatherData currentWeatherResponse;
    private WeatherForecast forecastResponse;
    private WeatherAggregatorService.WeatherReport reportResponse;

    @Given("the weather service is up and running")
    public void theWeatherServiceIsUpAndRunning() {
        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @When("I request current weather for {string}")
    public void iRequestCurrentWeatherFor(String location) {
        currentWeatherResponse = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/current")
                        .queryParam("location", location)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(WeatherData.class)
                .returnResult()
                .getResponseBody();
    }

    @When("I request a forecast for {string} for the next {int} days")
    public void iRequestAForecastForForTheNextDays(String location, int days) {
        forecastResponse = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/forecast")
                        .queryParam("location", location)
                        .queryParam("days", days)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(WeatherForecast.class)
                .returnResult()
                .getResponseBody();
    }

    @When("I request a full weather report for {string} for the next {int} days")
    public void iRequestAFullWeatherReportForForTheNextDays(String location, int days) {
        reportResponse = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/report")
                        .queryParam("location", location)
                        .queryParam("days", days)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(WeatherAggregatorService.WeatherReport.class)
                .returnResult()
                .getResponseBody();
    }

    @Then("I should receive weather data for {string}")
    public void iShouldReceiveWeatherDataFor(String location) {
        assertNotNull(currentWeatherResponse);
        assertEquals(location, currentWeatherResponse.getLocation());
    }

    @Then("the temperature should be a valid number")
    public void theTemperatureShouldBeAValidNumber() {
        assertTrue(currentWeatherResponse.getTemperature() > -100 &&
                currentWeatherResponse.getTemperature() < 100);
    }

    @Then("I should receive forecast data for {string}")
    public void iShouldReceiveForecastDataFor(String location) {
        assertNotNull(forecastResponse);
        assertEquals(location, forecastResponse.getLocation());
    }

    @Then("the forecast should contain {int} days of data")
    public void theForecastShouldContainDaysOfData(int days) {
        assertNotNull(forecastResponse.getDailyForecasts());
        assertEquals(days, forecastResponse.getDailyForecasts().size());
    }

    @Then("I should receive a report containing current weather and forecast for {string}")
    public void iShouldReceiveAReportContainingCurrentWeatherAndForecastFor(String location) {
        assertNotNull(reportResponse);
        assertNotNull(reportResponse.getCurrentWeather());
        assertNotNull(reportResponse.getForecast());
        assertEquals(location, reportResponse.getCurrentWeather().getLocation());
        assertEquals(location, reportResponse.getForecast().getLocation());
    }
}