package com.example.weatherservice.service;

import com.example.weatherservice.model.WeatherData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class WeatherProvider1Service {

    private final WebClient.Builder webClientBuilder;

    @Value("${api.weather.provider1.url:https://api.weatherprovider1.com}")
    private String apiUrl;

    @Autowired
    public WeatherProvider1Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<WeatherData> getCurrentWeather(String location) {
        // In a real implementation, this would call the external API
        // For this example, we're simulating the response

        // For demonstration, we'll mock the response
        return Mono.just(new WeatherData(
                location,
                22.5,
                65.0,
                10.2,
                "Partly Cloudy",
                LocalDateTime.now(),
                "Provider1"
        ));

        // Real implementation would look like this:
        /*
        return webClientBuilder.build()
            .get()
            .uri(apiUrl + "/current?location={location}", location)
            .retrieve()
            .bodyToMono(WeatherData.class);
        */
    }
}