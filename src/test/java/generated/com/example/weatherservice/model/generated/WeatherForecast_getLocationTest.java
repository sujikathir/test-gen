package com.example.weatherservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherForecast_getLocationTest {

    private WeatherForecast weatherForecast;

    @BeforeEach
    public void setup() {
        weatherForecast = Mockito.mock(WeatherForecast.class);
        Mockito.when(weatherForecast.getLocation()).thenReturn("New York");
    }

    @Test
    public void testGetLocation() {
        assertEquals("New York", weatherForecast.getLocation());
    }
}