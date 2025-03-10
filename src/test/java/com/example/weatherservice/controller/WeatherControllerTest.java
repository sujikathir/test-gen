package com.example.weatherservice.controller;

import com.example.weatherservice.model.WeatherData;
import com.example.weatherservice.model.WeatherForecast;
import com.example.weatherservice.service.WeatherAggregatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WeatherAggregatorService weatherService;

    @Test
    void getCurrentWeather_ShouldReturnWeatherData() {
        // Arrange
        String location = "New York";
        WeatherData mockWeatherData = new WeatherData(
                location, 22.5, 65.0, 10.2, "Partly Cloudy",
                LocalDateTime.now(), "Provider1"
        );

        when(weatherService.getCurrentWeather(anyString()))
                .thenReturn(Mono.just(mockWeatherData));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/current")
                        .queryParam("location", location)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.location").isEqualTo(location)
                .jsonPath("$.temperature").isEqualTo(22.5)
                .jsonPath("$.provider").isEqualTo("Provider1");
    }

    @Test
    void getForecast_ShouldReturnWeatherForecast() {
        // Arrange
        String location = "New York";
        int days = 3;

        WeatherForecast.DailyForecast day1 = new WeatherForecast.DailyForecast(
                LocalDate.now().plusDays(1), 25.0, 15.0, "Sunny", 0.1
        );

        WeatherForecast mockForecast = new WeatherForecast(
                location, Arrays.asList(day1), "Provider2"
        );

        when(weatherService.getForecast(anyString(), anyInt()))
                .thenReturn(Mono.just(mockForecast));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/forecast")
                        .queryParam("location", location)
                        .queryParam("days", days)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.location").isEqualTo(location)
                .jsonPath("$.provider").isEqualTo("Provider2")
                .jsonPath("$.dailyForecasts[0].highTemp").isEqualTo(25.0);
    }

    @Test
    void getWeatherReport_ShouldReturnCombinedReport() {
        // Arrange
        String location = "New York";
        int days = 3;

        WeatherData mockWeatherData = new WeatherData(
                location, 22.5, 65.0, 10.2, "Partly Cloudy",
                LocalDateTime.now(), "Provider1"
        );

        WeatherForecast.DailyForecast day1 = new WeatherForecast.DailyForecast(
                LocalDate.now().plusDays(1), 25.0, 15.0, "Sunny", 0.1
        );

        WeatherForecast mockForecast = new WeatherForecast(
                location, Arrays.asList(day1), "Provider2"
        );

        WeatherAggregatorService.WeatherReport mockReport =
                new WeatherAggregatorService.WeatherReport(mockWeatherData, mockForecast);

        when(weatherService.getFullWeatherReport(anyString(), anyInt()))
                .thenReturn(Mono.just(mockReport));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/report")
                        .queryParam("location", location)
                        .queryParam("days", days)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.currentWeather.location").isEqualTo(location)
                .jsonPath("$.currentWeather.provider").isEqualTo("Provider1")
                .jsonPath("$.forecast.provider").isEqualTo("Provider2");
    }
}