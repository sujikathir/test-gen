package com.example.weatherservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherForecast_getProviderTest {
    private WeatherForecast weatherForecast;

    @BeforeEach
    public void setup() {
        weatherForecast = new WeatherForecast();
    }

    @Test
    public void testGetProvider() {
        String expectedProvider = "TestProvider";
        weatherForecast.setProvider(expectedProvider);

        String actualProvider = weatherForecast.getProvider();

        assertEquals(expectedProvider, actualProvider, "Provider should be equal to the set value");
    }
}