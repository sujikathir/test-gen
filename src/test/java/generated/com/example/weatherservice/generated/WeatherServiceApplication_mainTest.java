package com.example.weatherservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;

public class WeatherServiceApplication_mainTest {

    @Test
    public void main() {
        // Mock the SpringApplication
        SpringApplication mockSpringApplication = Mockito.mock(SpringApplication.class);
        
        String[] args = new String[]{"arg1", "arg2"};

        // Call the main method
        WeatherServiceApplication.main(args);

        // Verify that the run method was called once with correct parameters
        Mockito.verify(mockSpringApplication, times(1)).run(WeatherServiceApplication.class, args);
    }
}