package com.example.weatherservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherForecast_getDailyForecastsTest {

    @InjectMocks
    private WeatherForecast weatherForecast;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetDailyForecasts() {
        DailyForecast dailyForecast1 = new DailyForecast();
        DailyForecast dailyForecast2 = new DailyForecast();
        List<DailyForecast> expectedDailyForecasts = Arrays.asList(dailyForecast1, dailyForecast2);

        weatherForecast.setDailyForecasts(expectedDailyForecasts);
        List<DailyForecast> actualDailyForecasts = weatherForecast.getDailyForecasts();

        assertEquals(expectedDailyForecasts, actualDailyForecasts, "The returned daily forecasts should match the expected daily forecasts");
    }
}