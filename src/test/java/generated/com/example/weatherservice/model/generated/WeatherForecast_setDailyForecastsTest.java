package com.example.weatherservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherForecast_setDailyForecastsTest {

    private WeatherForecast weatherForecast;
    private DailyForecast mockDailyForecast;

    @BeforeEach
    public void setUp() {
        weatherForecast = new WeatherForecast();
        mockDailyForecast = Mockito.mock(DailyForecast.class);
    }

    @Test
    public void testSetDailyForecasts() {
        List<DailyForecast> dailyForecasts = Arrays.asList(mockDailyForecast, mockDailyForecast);
        weatherForecast.setDailyForecasts(dailyForecasts);
        assertEquals(dailyForecasts, weatherForecast.getDailyForecasts());
    }
}