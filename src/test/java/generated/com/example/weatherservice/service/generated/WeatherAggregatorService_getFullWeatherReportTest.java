package com.example.weatherservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import com.example.weatherservice.model.WeatherReport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WeatherAggregatorService_getFullWeatherReportTest {

    @Mock
    private WeatherAggregatorService weatherAggregatorService;

    @InjectMocks
    private WeatherAggregatorService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getFullWeatherReport() {
        String location = "London";
        int forecastDays = 5;

        when(weatherAggregatorService.getCurrentWeather(any(String.class))).thenReturn(Mono.just(new WeatherReport()));
        when(weatherAggregatorService.getForecast(any(String.class), anyInt())).thenReturn(Mono.just(new WeatherReport()));

        Mono<WeatherReport> weatherReportMono = service.getFullWeatherReport(location, forecastDays);

        assertNotNull(weatherReportMono);
        assertNotNull(weatherReportMono.block());
    }
}