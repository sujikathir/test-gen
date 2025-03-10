package com.example.weatherservice.controller;

import com.example.weatherservice.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class WeatherController_getCurrentWeatherTest {

    @InjectMocks
    private WeatherController weatherController;

    @Mock
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getCurrentWeather_success() {
        String location = "TestLocation";
        WeatherData weatherData = new WeatherData();
        when(weatherService.getCurrentWeather(location)).thenReturn(Mono.just(weatherData));

        Mono<ResponseEntity<WeatherData>> response = weatherController.getCurrentWeather(location);

        assertEquals(ResponseEntity.ok().body(weatherData), response.block());
    }

    @Test
    void getCurrentWeather_notFound() {
        String location = "TestLocation";
        when(weatherService.getCurrentWeather(location)).thenReturn(Mono.empty());

        Mono<ResponseEntity<WeatherData>> response = weatherController.getCurrentWeather(location);

        assertEquals(ResponseEntity.notFound().build(), response.block());
    }
}