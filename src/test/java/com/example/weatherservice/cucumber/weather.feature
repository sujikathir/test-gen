Feature: Weather Service API
  As a user of the weather service
  I want to get current weather and forecast information
  So that I can plan my activities accordingly

  Scenario: Get current weather for a location
    Given the weather service is up and running
    When I request current weather for "New York"
    Then I should receive weather data for "New York"
    And the temperature should be a valid number

  Scenario: Get forecast for a location
    Given the weather service is up and running
    When I request a forecast for "New York" for the next 3 days
    Then I should receive forecast data for "New York"
    And the forecast should contain 3 days of data

  Scenario: Get full weather report for a location
    Given the weather service is up and running
    When I request a full weather report for "New York" for the next 3 days
    Then I should receive a report containing current weather and forecast for "New York"