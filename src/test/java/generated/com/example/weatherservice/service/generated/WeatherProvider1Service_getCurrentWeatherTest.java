package com.example.weatherservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

public class WeatherProvider1Service_getCurrentWeatherTest {

    @InjectMocks
    private WeatherProvider1Service weatherProvider1Service;

    @Mock
    private WeatherData weatherData;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        weatherData = new WeatherData("London", 22.5, 65.0, 10.2, "Partly Cloudy", LocalDateTime.now(), "Provider1");
        when(weatherProvider1Service.getCurrentWeather("London")).thenReturn(Mono.just(weatherData));
    }

    @Test
    public void testGetCurrentWeather() {
        Mono<WeatherData> weatherDataMono = weatherProvider1Service.getCurrentWeather("London");

        StepVerifier.create(weatherDataMono)
                .expectNextMatches(weatherData -> {
                    assert weatherData.getLocation().equals("London");
                    assert weatherData.getTemperature() == 22.5;
                    assert weatherData.getHumidity() == 65.0;
                    assert weatherData.getWindSpeed() == 10.2;
                    assert weatherData.getWeatherCondition().equals("Partly Cloudy");
                    assert weatherData.getTimestamp() != null;
                    assert weatherData.getProvider().equals("Provider1");
                    return true;
                })
                .verifyComplete();
    }
}