package com.example.weatherservice.service;

import reactor.core.publisher.Mono;
import com.example.weatherservice.dto.WeatherData;
import com.example.weatherservice.service.WeatherProvider1Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherAggregatorService_getCurrentWeatherTest {

    private WeatherAggregatorService weatherAggregatorService;

    @Mock
    private WeatherProvider1Service weatherProvider1Service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        weatherAggregatorService = new WeatherAggregatorService(weatherProvider1Service);
    }

    @Test
    public void getCurrentWeatherTest() {
        String location = "Test Location";
        WeatherData expectedWeatherData = new WeatherData();
        given(weatherProvider1Service.getCurrentWeather(location)).willReturn(Mono.just(expectedWeatherData));

        Mono<WeatherData> actualWeatherData = weatherAggregatorService.getCurrentWeather(location);

        assertEquals(expectedWeatherData, actualWeatherData.block());
    }
}