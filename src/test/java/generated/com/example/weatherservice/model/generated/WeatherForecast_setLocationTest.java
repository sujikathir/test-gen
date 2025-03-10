package com.example.weatherservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WeatherForecast_setLocationTest {

    private WeatherForecast weatherForecast;

    @BeforeEach
    public void setUp() {
        weatherForecast = new WeatherForecast();
    }

    @Test
    public void testSetLocation() {
        String location = "Los Angeles, CA";
        weatherForecast.setLocation(location);
        assertEquals(location, weatherForecast.getLocation());
    }
}