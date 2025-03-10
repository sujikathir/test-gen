package com.example.weatherservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceApplication_webClientBuilderTest {

    @InjectMocks
    private WeatherServiceApplication weatherServiceApplication;

    private WebClient.Builder webClientBuilder;

    @BeforeEach
    public void setup() {
        webClientBuilder = weatherServiceApplication.webClientBuilder();
    }

    @Test
    public void testWebClientBuilder() {
        assertNotNull(webClientBuilder, "WebClient builder should not be null");
        assertTrue(webClientBuilder instanceof WebClient.Builder, "Object should be instance of WebClient.Builder");
    }
}