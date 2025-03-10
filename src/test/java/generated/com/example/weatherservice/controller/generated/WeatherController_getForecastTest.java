package com.example.weatherservice.controller;

import com.example.weatherservice.model.WeatherForecast;
import com.example.weatherservice.service.WeatherAggregatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class WeatherController_getForecastTest {

    @InjectMocks
    private WeatherController weatherController;

    @Mock
    private WeatherAggregatorService weatherService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getForecast_NotFound() {
        when(weatherService.getForecast(anyString(), anyInt())).thenReturn(Mono.empty());

        Mono<ResponseEntity<WeatherForecast>> forecast =
            weatherController.getForecast("location", 5);

        StepVerifier.create(forecast)
            .expectNextMatches(response -> response.getStatusCode().is4xxClientError())
            .verifyComplete();
    }

    @Test
    public void getForecast_Found() {
        WeatherForecast weatherForecast = new WeatherForecast();
        when(weatherService.getForecast(anyString(), anyInt())).thenReturn(Mono.just(weatherForecast));

        Mono<ResponseEntity<WeatherForecast>> forecast =
            weatherController.getForecast("location", 5);

        StepVerifier.create(forecast)
            .expectNextMatches(response -> 
                response.getStatusCode().is2xxSuccessful() && response.getBody().equals(weatherForecast))
            .verifyComplete();
    }
}