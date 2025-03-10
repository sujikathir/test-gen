package com.example.weatherservice.service;

import com.example.weatherservice.model.WeatherData;
import com.example.weatherservice.model.WeatherForecast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class WeatherAggregatorService {

    private final WeatherProvider1Service weatherProvider1Service;
    private final WeatherProvider2Service weatherProvider2Service;

    @Autowired
    public WeatherAggregatorService(
            WeatherProvider1Service weatherProvider1Service,
            WeatherProvider2Service weatherProvider2Service
    ) {
        this.weatherProvider1Service = weatherProvider1Service;
        this.weatherProvider2Service = weatherProvider2Service;
    }

    public Mono<WeatherData> getCurrentWeather(String location) {
        return weatherProvider1Service.getCurrentWeather(location);
    }

    public Mono<WeatherForecast> getForecast(String location, int days) {
        return weatherProvider2Service.getForecast(location, days);
    }

    public Mono<WeatherReport> getFullWeatherReport(String location, int forecastDays) {
        return Mono.zip(
                getCurrentWeather(location),
                getForecast(location, forecastDays)
        ).map(this::combineWeatherData);
    }

    private WeatherReport combineWeatherData(Tuple2<WeatherData, WeatherForecast> tuple) {
        WeatherData currentWeather = tuple.getT1();
        WeatherForecast forecast = tuple.getT2();

        return new WeatherReport(currentWeather, forecast);
    }

    public static class WeatherReport {
        private WeatherData currentWeather;
        private WeatherForecast forecast;

        public WeatherReport(WeatherData currentWeather, WeatherForecast forecast) {
            this.currentWeather = currentWeather;
            this.forecast = forecast;
        }

        public WeatherData getCurrentWeather() {
            return currentWeather;
        }

        public void setCurrentWeather(WeatherData currentWeather) {
            this.currentWeather = currentWeather;
        }

        public WeatherForecast getForecast() {
            return forecast;
        }

        public void setForecast(WeatherForecast forecast) {
            this.forecast = forecast;
        }
    }
}