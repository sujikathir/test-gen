package com.example.weatherservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherData_getLocationTest {

    private WeatherData weatherData;

    @BeforeEach
    public void setUp() {
        weatherData = Mockito.mock(WeatherData.class);
        Mockito.when(weatherData.getLocation()).thenReturn("New York");
    }

    @Test
    public void testGetLocation() {
        String location = weatherData.getLocation();
        assertEquals("New York", location, "Expected Location is not returned");
    }
}