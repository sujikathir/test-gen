package com.example.weatherservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.util.function.Tuple2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class WeatherAggregatorService_combineWeatherDataTest {

    private WeatherAggregatorService weatherAggregatorService;
    private Tuple2<WeatherData, WeatherForecast> weatherDataTuple;

    @BeforeEach
    void setUp() {
        weatherAggregatorService = new WeatherAggregatorService();
        weatherDataTuple = mock(Tuple2.class);
    }

    @Test
    void testCombineWeatherData() {
        WeatherData mockWeatherData = mock(WeatherData.class);
        WeatherForecast mockWeatherForecast = mock(WeatherForecast.class);

        Mockito.when(weatherDataTuple.getT1()).thenReturn(mockWeatherData);
        Mockito.when(weatherDataTuple.getT2()).thenReturn(mockWeatherForecast);

        WeatherReport weatherReport = weatherAggregatorService.combineWeatherData(weatherDataTuple);

        assertEquals(mockWeatherData, weatherReport.getCurrentWeather());
        assertEquals(mockWeatherForecast, weatherReport.getForecast());
    }
}