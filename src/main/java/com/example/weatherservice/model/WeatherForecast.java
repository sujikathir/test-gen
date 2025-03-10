package com.example.weatherservice.model;

import java.time.LocalDate;
import java.util.List;

public class WeatherForecast {
    private String location;
    private List<DailyForecast> dailyForecasts;
    private String provider;

    public WeatherForecast() {
    }

    public WeatherForecast(String location, List<DailyForecast> dailyForecasts, String provider) {
        this.location = location;
        this.dailyForecasts = dailyForecasts;
        this.provider = provider;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<DailyForecast> getDailyForecasts() {
        return dailyForecasts;
    }

    public void setDailyForecasts(List<DailyForecast> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public static class DailyForecast {
        private LocalDate date;
        private double highTemp;
        private double lowTemp;
        private String condition;
        private double precipitationChance;

        public DailyForecast() {
        }

        public DailyForecast(LocalDate date, double highTemp, double lowTemp,
                             String condition, double precipitationChance) {
            this.date = date;
            this.highTemp = highTemp;
            this.lowTemp = lowTemp;
            this.condition = condition;
            this.precipitationChance = precipitationChance;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public double getHighTemp() {
            return highTemp;
        }

        public void setHighTemp(double highTemp) {
            this.highTemp = highTemp;
        }

        public double getLowTemp() {
            return lowTemp;
        }

        public void setLowTemp(double lowTemp) {
            this.lowTemp = lowTemp;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public double getPrecipitationChance() {
            return precipitationChance;
        }

        public void setPrecipitationChance(double precipitationChance) {
            this.precipitationChance = precipitationChance;
        }
    }
}