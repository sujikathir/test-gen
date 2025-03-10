package com.example.weatherservice.controller;

import com.example.weatherservice.model.WeatherData;
import com.example.weatherservice.model.WeatherForecast;
import com.example.weatherservice.service.WeatherAggregatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherAggregatorService weatherService;

    @Autowired
    public WeatherController(WeatherAggregatorService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public Mono<ResponseEntity<WeatherData>> getCurrentWeather(
            @RequestParam String location) {
        return weatherService.getCurrentWeather(location)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/forecast")
    public Mono<ResponseEntity<WeatherForecast>> getForecast(
            @RequestParam String location,
            @RequestParam(defaultValue = "5") int days) {
        return weatherService.getForecast(location, days)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/report")
    public Mono<ResponseEntity<WeatherAggregatorService.WeatherReport>> getWeatherReport(
            @RequestParam String location,
            @RequestParam(defaultValue = "5") int days) {
        return weatherService.getFullWeatherReport(location, days)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}