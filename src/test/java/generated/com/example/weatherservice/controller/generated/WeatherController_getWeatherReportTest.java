package com.example.weatherservice.controller;

import com.example.weatherservice.model.WeatherData;
import com.example.weatherservice.service.WeatherAggregatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class WeatherController_getWeatherReportTest {

    @Mock
    private WeatherAggregatorService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getWeatherReport_whenWeatherReportIsNotEmpty() {
        WeatherAggregatorService.WeatherReport weatherReport = new WeatherAggregatorService.WeatherReport();
        when(weatherService.getFullWeatherReport(anyString(), anyInt())).thenReturn(Mono.just(weatherReport));

        StepVerifier.create(weatherController.getWeatherReport("London", 5))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.OK
                        && responseEntity.getBody() == weatherReport)
                .verifyComplete();
    }

    @Test
    void getWeatherReport_whenWeatherReportIsEmpty() {
        when(weatherService.getFullWeatherReport(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(weatherController.getWeatherReport("London", 5))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }
}