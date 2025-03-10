package com.example.weatherservice.service;

import com.example.weatherservice.model.WeatherForecast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;

@Service
public class WeatherProvider2Service {

    private final WebClient.Builder webClientBuilder;

    @Value("${api.weather.provider2.url:https://api.weatherprovider2.com}")
    private String apiUrl;

    @Autowired
    public WeatherProvider2Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<WeatherForecast> getForecast(String location, int days) {
        // In a real implementation, this would call the external API
        // For this example, we're simulating the response

        // Mock forecast data
        WeatherForecast.DailyForecast day1 = new WeatherForecast.DailyForecast(
                LocalDate.now().plusDays(1),
                25.0,
                15.0,
                "Sunny",
                0.1
        );

        WeatherForecast.DailyForecast day2 = new WeatherForecast.DailyForecast(
                LocalDate.now().plusDays(2),
                23.5,
                14.0,
                "Partly Cloudy",
                0.3
        );

        WeatherForecast.DailyForecast day3 = new WeatherForecast.DailyForecast(
                LocalDate.now().plusDays(3),
                21.0,
                13.5,
                "Rain",
                0.7
        );

        // Create and return forecast
        return Mono.just(new WeatherForecast(
                location,
                Arrays.asList(day1, day2, day3),
                "Provider2"
        ));

        // Real implementation would look like this:
        /*
        return webClientBuilder.build()
            .get()
            .uri(apiUrl + "/forecast?location={location}&days={days}", location, days)
            .retrieve()
            .bodyToMono(WeatherForecast.class);
        */
    }
}