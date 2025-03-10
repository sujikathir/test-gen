// Configure JaCoCo
project.jacoco {
    toolVersion = "0.8.8"
}

project.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

// Add test generation tasks
