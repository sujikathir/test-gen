package com.example.weatherservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherForecast_setProviderTest {
    private WeatherForecast weatherForecast;

    @BeforeEach
    public void setUp() {
        weatherForecast = new WeatherForecast();
    }

    @Test
    public void testSetProvider() {
        String expectedProvider = "Test Provider";
        weatherForecast.setProvider(expectedProvider);
        assertEquals(expectedProvider, weatherForecast.getProvider());
    }
}