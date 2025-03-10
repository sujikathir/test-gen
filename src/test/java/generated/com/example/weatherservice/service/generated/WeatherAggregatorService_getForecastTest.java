package com.example.weatherservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import com.example.weatherservice.model.WeatherForecast;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WeatherAggregatorService_getForecastTest {

    @Mock
    private WeatherProvider2Service weatherProvider2Service;

    private WeatherAggregatorService weatherAggregatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        weatherAggregatorService = new WeatherAggregatorService(weatherProvider2Service);
    }

    @Test
    void getForecastTest() {
        when(weatherProvider2Service.getForecast(anyString(), anyInt())).thenReturn(Mono.just(new WeatherForecast()));
        Mono<WeatherForecast> result = weatherAggregatorService.getForecast("London", 5);
        assertNotNull(result.block());
    }
}