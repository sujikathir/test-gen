package com.example.weatherservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherProvider2Service_getForecastTest {

    private WeatherProvider2Service weatherProvider2Service;

    @BeforeEach
    void setUp() {
        weatherProvider2Service = Mockito.spy(new WeatherProvider2Service());
    }

    @Test
    void getForecastTest() {
        String location = "TestLocation";
        int days = 3;

        WeatherForecast.DailyForecast day1 = new WeatherForecast.DailyForecast(
                LocalDate.now().plusDays(1),
                25.0,
                15.0,
                "Sunny",
                0.1
        );

        WeatherForecast.DailyForecast day2 = new WeatherForecast.DailyForecast(
                LocalDate.now().plusDays(2),
                23.5,
                14.0,
                "Partly Cloudy",
                0.3
        );

        WeatherForecast.DailyForecast day3 = new WeatherForecast.DailyForecast(
                LocalDate.now().plusDays(3),
                21.0,
                13.5,
                "Rain",
                0.7
        );

        Mono<WeatherForecast> weatherForecast = weatherProvider2Service.getForecast(location, days);

        StepVerifier.create(weatherForecast)
                .assertNext(forecast -> {
                    assertEquals(location, forecast.getLocation());
                    assertEquals("Provider2", forecast.getProvider());
                    assertEquals(Arrays.asList(day1, day2, day3), forecast.getDailyForecasts());
                })
                .verifyComplete();
    }
}