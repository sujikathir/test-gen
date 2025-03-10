package com.example.weatherservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WeatherServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();

        configureFor("localhost", wireMockServer.port());

        // Configure mock for provider 1
        stubFor(WireMock.get(urlPathMatching("/provider1/current.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"location\":\"New York\",\"temperature\":22.5,\"humidity\":65.0,\"windSpeed\":10.2,\"condition\":\"Partly Cloudy\",\"timestamp\":\"2023-11-28T12:00:00\",\"provider\":\"Provider1\"}")
                ));

        // Configure mock for provider 2
        stubFor(WireMock.get(urlPathMatching("/provider2/forecast.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"location\":\"New York\",\"dailyForecasts\":[{\"date\":\"2023-11-29\",\"highTemp\":25.0,\"lowTemp\":15.0,\"condition\":\"Sunny\",\"precipitationChance\":0.1}],\"provider\":\"Provider2\"}")
                ));

        // Override the API URLs to use WireMock server
        System.setProperty("api.weather.provider1.url", "http://localhost:" + wireMockServer.port() + "/provider1");
        System.setProperty("api.weather.provider2.url", "http://localhost:" + wireMockServer.port() + "/provider2");
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
        System.clearProperty("api.weather.provider1.url");
        System.clearProperty("api.weather.provider2.url");
    }

    @Test
    void getCurrentWeather_ShouldReturnDataFromProvider1() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/current")
                        .queryParam("location", "New York")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.location").isEqualTo("New York")
                .jsonPath("$.temperature").isEqualTo(22.5)
                .jsonPath("$.provider").isEqualTo("Provider1");

        // Verify that the mock was called
        verify(getRequestedFor(urlPathMatching("/provider1/current.*")));
    }

    @Test
    void getForecast_ShouldReturnDataFromProvider2() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/forecast")
                        .queryParam("location", "New York")
                        .queryParam("days", 3)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.location").isEqualTo("New York")
                .jsonPath("$.provider").isEqualTo("Provider2")
                .jsonPath("$.dailyForecasts[0].highTemp").isEqualTo(25.0);

        // Verify that the mock was called
        verify(getRequestedFor(urlPathMatching("/provider2/forecast.*")));
    }

    @Test
    void getWeatherReport_ShouldReturnCombinedReportFromBothProviders() {
        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather/report")
                        .queryParam("location", "New York")
                        .queryParam("days", 3)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.currentWeather.location").isEqualTo("New York")
                .jsonPath("$.currentWeather.provider").isEqualTo("Provider1")
                .jsonPath("$.forecast.provider").isEqualTo("Provider2");

        // Verify that both mocks were called
        verify(getRequestedFor(urlPathMatching("/provider1/current.*")));
        verify(getRequestedFor(urlPathMatching("/provider2/forecast.*")));
    }
}