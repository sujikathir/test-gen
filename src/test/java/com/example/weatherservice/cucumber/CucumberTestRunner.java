package com.example.weatherservice.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.example.weatherservice.cucumber",
        plugin = {"pretty", "html:target/cucumber-reports"}
)
public class CucumberTestRunner {
}