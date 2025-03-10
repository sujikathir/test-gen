# Java Test Generation Framework

## Overview

This project provides a framework for automatically generating unit tests for Java projects using AI. It analyzes code coverage to identify testing gaps, extracts the relevant code, and leverages AI models (like OpenAI's GPT or Anthropic's Claude) to generate appropriate test cases.

## Features

- **Coverage Analysis**: Automatically analyzes JaCoCo coverage reports to identify methods with insufficient test coverage
- **AI-Powered Test Generation**: Generates unit tests using either OpenAI or AWS Bedrock (Claude) APIs
- **Multiple AI Providers**: Supports both OpenAI (GPT models) and AWS Bedrock (Claude models)
- **Smart Prompting**: Crafts specialized prompts describing the code and coverage gaps
- **Selective Analysis**: Can focus on specific packages or exclude certain patterns
- **Customizable Thresholds**: Configure the minimum coverage threshold for test generation

## Prerequisites

- Java 11 or higher
- Gradle or Maven project with JaCoCo configured
- OpenAI API key or AWS Bedrock credentials
- Existing test coverage data (JaCoCo .exec file)

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/
│   │   │       └── testgen/
│   │   │           └── core/
│   │   │               └── GenericTestGenerator.java
│   └── test/
│       └── java/
│           └── generated/  (Where generated tests will be saved)
├── build.gradle
└── testgen.json  (Configuration file)
```

## Installation

1. Create the following directory structure in your project:
   ```
   src/main/java/io/testgen/core/
   ```

2. Copy the `GenericTestGenerator.java` file into this directory.

3. Create a `testgen.json` configuration file in your project root.

4. Add the following tasks to your `build.gradle` file:

```groovy
task analyzeTestCoverage(type: JavaExec) {
    description = 'Analyzes test coverage and identifies gaps'
    group = 'Test Generation'
    
    classpath = sourceSets.main.runtimeClasspath + sourceSets.test.runtimeClasspath
    mainClass = 'io.testgen.core.GenericTestGenerator'
    
    dependsOn test, jacocoTestReport
}

task generateAiTests(dependsOn: analyzeTestCoverage) {
    description = 'Generates tests using AI based on coverage analysis'
    group = 'Test Generation'
}
```

## Configuration

Create a `testgen.json` file in your project root with the following structure:

```json
{
  "coverage": {
    "execFile": "build/jacoco/test.exec",
    "classDir": "build/classes/java/main",
    "sourceDir": "src/main/java",
    "threshold": 80
  },
  "aiProvider": {
    "type": "openai",
    "apiKey": "YOUR_OPENAI_API_KEY",
    "model": "gpt-4"
  },
  "output": {
    "dir": "src/test/java/generated"
  },
  "targetPackages": [
    "com.example.*"
  ],
  "exclusions": [
    "**/*Application",
    "**/*Config",
    "**/*Exception"
  ]
}
```

### Configuration Options

- **coverage**: Settings related to code coverage analysis
    - **execFile**: Path to the JaCoCo execution data file
    - **classDir**: Path to compiled classes
    - **sourceDir**: Path to source code
    - **threshold**: Coverage percentage threshold (methods below this will have tests generated)

- **aiProvider**: AI service configuration
    - **type**: `"openai"` or `"bedrock"`
    - **apiKey**: For OpenAI, your API key
    - **awsAccessKeyId** and **awsSecretKey**: For Bedrock, your AWS credentials
    - **region**: For Bedrock, AWS region (default: "us-east-1")
    - **model**: Model to use (e.g., "gpt-4" for OpenAI or "anthropic.claude-3-sonnet-20240229-v1:0" for Bedrock)

- **output**: Output settings
    - **dir**: Directory where generated tests will be saved

- **targetPackages**: List of package patterns to include (empty means all)
- **exclusions**: List of class patterns to exclude

## Usage

### Basic Usage

1. Run your existing tests with JaCoCo to generate coverage data:
   ```
   ./gradlew test jacocoTestReport
   ```

2. Generate tests for methods with insufficient coverage:
   ```
   ./gradlew generateAiTests
   ```

3. Alternatively, skip running tests if you already have coverage data:
   ```
   ./gradlew -x test generateAiTests
   ```

### Advanced Usage

#### Using OpenAI (Recommended)

1. Update your `testgen.json` with your OpenAI API key:
   ```json
   "aiProvider": {
     "type": "openai",
     "apiKey": "YOUR_OPENAI_API_KEY",
     "model": "gpt-4"
   }
   ```

2. Run the test generator:
   ```
   ./gradlew -x test generateAiTests
   ```

#### Using AWS Bedrock (Claude)

1. Update your `testgen.json` with your AWS credentials:
   ```json
   "aiProvider": {
     "type": "bedrock",
     "awsAccessKeyId": "YOUR_AWS_ACCESS_KEY",
     "awsSecretKey": "YOUR_AWS_SECRET_KEY",
     "region": "us-east-1",
     "model": "anthropic.claude-3-sonnet-20240229-v1:0"
   }
   ```

2. Run the test generator:
   ```
   ./gradlew -x test generateAiTests
   ```

#### Focusing on Specific Packages

To focus on specific packages, update the `targetPackages` in your configuration:

```json
"targetPackages": [
  "com.example.service.*",
  "com.example.controller.*"
]
```

#### Excluding Certain Classes

To exclude specific types of classes, update the `exclusions` in your configuration:

```json
"exclusions": [
  "**/*Application",
  "**/*Config",
  "**/*Factory",
  "**/*Util"
]
```

## How It Works

The test generator follows this process:

1. **Analyzes Coverage**: Reads JaCoCo execution data to identify methods with coverage below the threshold
2. **Extracts Source Code**: Finds and extracts the relevant method code
3. **Creates AI Prompts**: Crafts detailed prompts describing the method and its coverage gaps
4. **Calls AI API**: Sends prompts to the configured AI provider
5. **Processes AI Response**: Extracts the generated test code from the AI response
6. **Saves Tests**: Writes the generated tests to the specified output directory

## Troubleshooting

### Common Issues

1. **Missing coverage data**:
    - Make sure you've run `./gradlew test jacocoTestReport` first
    - Check that the JaCoCo exec file path in `testgen.json` is correct

2. **API authentication errors**:
    - For OpenAI: Verify your API key is correct
    - For Bedrock: Ensure your AWS credentials have Bedrock access

3. **Generated tests have compilation errors**:
    - The AI might not have complete context about your codebase
    - Edit the generated tests or regenerate with improved prompts

4. **No tests are generated**:
    - Check that your coverage threshold isn't too high
    - Verify that there are methods with coverage below the threshold

## Extending the Framework

### Supporting New AI Providers

To add support for a new AI service:

1. Add a new method to `GenericTestGenerator.java` (e.g., `generateWithNewAI(String prompt)`)
2. Update the `generateTestWithAI` method to handle the new provider type
3. Implement the necessary API calls and response parsing

### Customizing Prompts

The prompt generation logic is in the `createPrompt` method. You can modify this to:
- Include more context about your codebase
- Add specific testing requirements
- Adjust the format of generated tests


