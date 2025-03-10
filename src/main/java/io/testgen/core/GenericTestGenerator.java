package io.testgen.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Generic AI-powered test generator that can be used with any Java project.
 * Uses test coverage analysis to identify gaps and leverages AI to generate
 * appropriate test cases.
 */
public class GenericTestGenerator {
    private static final String CONFIG_FILE = "testgen.json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private JsonNode config;
    private String projectRoot;

    static {
        // Disable Spring Boot's default logging system to avoid conflicts
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
    }

    /**
     * Initialize the test generator with default settings.
     */
    public GenericTestGenerator() throws IOException {
        this(null);
    }

    /**
     * Initialize the test generator with a specific configuration file.
     *
     * @param configFile Path to config file, or null to use default
     */
    public GenericTestGenerator(String configFile) throws IOException {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();

        // Find project root directory
        this.projectRoot = findProjectRoot();

        // Load configuration
        String configPath = configFile != null ? configFile : Paths.get(projectRoot, CONFIG_FILE).toString();
        if (Files.exists(Paths.get(configPath))) {
            this.config = objectMapper.readTree(new File(configPath));
        } else {
            // Create default configuration
            this.config = createDefaultConfig();
            // Save default config
            Files.writeString(Paths.get(configPath),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config));
            System.out.println("Created default configuration at: " + configPath);
        }
    }

    /**
     * Find the project root directory by looking for build.gradle, pom.xml, etc.
     */
    private String findProjectRoot() {
        Path currentPath = Paths.get("").toAbsolutePath();

        while (currentPath != null) {
            if (Files.exists(currentPath.resolve("build.gradle")) ||
                    Files.exists(currentPath.resolve("pom.xml"))) {
                return currentPath.toString();
            }
            currentPath = currentPath.getParent();
        }

        // Fallback to current directory
        return Paths.get("").toAbsolutePath().toString();
    }

    /**
     * Create default configuration.
     */
    private JsonNode createDefaultConfig() {
        ObjectNode config = objectMapper.createObjectNode();

        // Coverage settings
        ObjectNode coverage = objectMapper.createObjectNode();
        coverage.put("execFile", "build/jacoco/test.exec");
        coverage.put("classDir", "build/classes/java/main");
        coverage.put("sourceDir", "src/main/java");
        coverage.put("threshold", 80);
        config.set("coverage", coverage);

        // AI provider settings
        ObjectNode aiProvider = objectMapper.createObjectNode();
        aiProvider.put("type", "openai");
        aiProvider.put("apiKey", "YOUR_API_KEY_HERE");
        aiProvider.put("model", "gpt-4");
        config.set("aiProvider", aiProvider);

        // Output settings
        ObjectNode output = objectMapper.createObjectNode();
        output.put("dir", "src/test/java/generated");
        config.set("output", output);

        // Target packages or classes to analyze (empty means all)
        ArrayNode targets = objectMapper.createArrayNode();
        config.set("targetPackages", targets);

        // Exclusions
        ArrayNode exclusions = objectMapper.createArrayNode();
        exclusions.add("**/*Application");
        exclusions.add("**/*Config");
        exclusions.add("**/*Exception");
        config.set("exclusions", exclusions);

        return config;
    }

    /**
     * Run the test generation process.
     */
    public void run() throws IOException {
        System.out.println("Starting test generation process...");

        // Step 1: Analyze code coverage
        Map<String, List<CoverageGap>> coverageGaps = analyzeCoverage();
        if (coverageGaps.isEmpty()) {
            System.out.println("No coverage gaps found. Exiting.");
            return;
        }

        // Step 2: For each gap, generate test using AI
        for (Map.Entry<String, List<CoverageGap>> entry : coverageGaps.entrySet()) {
            String className = entry.getKey();
            List<CoverageGap> gaps = entry.getValue();

            System.out.println("Generating tests for: " + className);

            String sourceCode = getSourceCode(className);
            if (sourceCode.isEmpty()) {
                System.out.println("  Could not find source code. Skipping.");
                continue;
            }

            for (CoverageGap gap : gaps) {
                System.out.println("  - Method: " + gap.methodName);

                String generatedTest = generateTestWithAI(className, gap, sourceCode);
                if (generatedTest != null && !generatedTest.isEmpty()) {
                    saveGeneratedTest(className, gap.methodName, generatedTest);
                }

                // Sleep to avoid rate limiting
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("Test generation complete!");
    }

    /**
     * Analyze code coverage and identify gaps.
     */
    private Map<String, List<CoverageGap>> analyzeCoverage() throws IOException {
        Map<String, List<CoverageGap>> coverageGaps = new HashMap<>();

        String execFilePath = config.get("coverage").get("execFile").asText();
        File execFile = Paths.get(projectRoot, execFilePath).toFile();

        if (!execFile.exists()) {
            System.err.println("Coverage file not found: " + execFile.getAbsolutePath());
            System.err.println("Run your tests with JaCoCo enabled first.");
            return coverageGaps;
        }

        try {
            ExecFileLoader loader = new ExecFileLoader();
            loader.load(execFile);

            ExecutionDataStore executionData = loader.getExecutionDataStore();
            SessionInfoStore sessionInfo = loader.getSessionInfoStore();

            CoverageBuilder coverageBuilder = new CoverageBuilder();
            String classDir = config.get("coverage").get("classDir").asText();
            File classDirFile = Paths.get(projectRoot, classDir).toFile();

            Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
            analyzer.analyzeAll(classDirFile);

            int threshold = config.get("coverage").get("threshold").asInt(80);

            // Process each class
            for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {
                String className = classCoverage.getName().replace('/', '.');

                // Skip excluded classes
                if (isExcluded(className)) {
                    continue;
                }

                // Skip if not in target packages (if specified)
                if (!isInTargetPackages(className)) {
                    continue;
                }

                List<CoverageGap> gaps = new ArrayList<>();

                // Check each method
                for (IMethodCoverage methodCoverage : classCoverage.getMethods()) {
                    // Skip constructors and static initializers
                    if ("<init>".equals(methodCoverage.getName()) ||
                            "<clinit>".equals(methodCoverage.getName())) {
                        continue;
                    }

                    // Calculate coverage
                    int coveredInstructions = methodCoverage.getInstructionCounter().getCoveredCount();
                    int totalInstructions = methodCoverage.getInstructionCounter().getTotalCount();
                    int coveredBranches = methodCoverage.getBranchCounter().getCoveredCount();
                    int totalBranches = methodCoverage.getBranchCounter().getTotalCount();

                    if (totalInstructions == 0) {
                        continue;
                    }

                    double instructionCoverage = (double) coveredInstructions / totalInstructions * 100;
                    double branchCoverage = totalBranches > 0 ?
                            (double) coveredBranches / totalBranches * 100 : 100.0;

                    // If coverage is below threshold, add to gaps
                    if (instructionCoverage < threshold || (totalBranches > 0 && branchCoverage < threshold)) {
                        gaps.add(new CoverageGap(
                                methodCoverage.getName(),
                                methodCoverage.getDesc(),
                                instructionCoverage,
                                branchCoverage,
                                identifyMissingCases(methodCoverage),
                                methodCoverage.getFirstLine(),
                                methodCoverage.getLastLine()
                        ));
                    }
                }

                if (!gaps.isEmpty()) {
                    coverageGaps.put(className, gaps);
                }
            }

        } catch (Exception e) {
            System.err.println("Error analyzing coverage: " + e.getMessage());
            e.printStackTrace();
        }

        return coverageGaps;
    }

    /**
     * Check if a class is excluded.
     */
    private boolean isExcluded(String className) {
        JsonNode exclusions = config.get("exclusions");
        if (exclusions != null && exclusions.isArray()) {
            for (JsonNode exclusion : exclusions) {
                String pattern = exclusion.asText();
                if (matchesPattern(className, pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a class is in target packages.
     */
    private boolean isInTargetPackages(String className) {
        JsonNode targetPackages = config.get("targetPackages");

        // If no targets specified, include all
        if (targetPackages == null || !targetPackages.isArray() || targetPackages.size() == 0) {
            return true;
        }

        for (JsonNode targetPackage : targetPackages) {
            String pattern = targetPackage.asText();
            if (matchesPattern(className, pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a class name matches a pattern.
     */
    private boolean matchesPattern(String className, String pattern) {
        if (pattern.endsWith("*")) {
            return className.startsWith(pattern.substring(0, pattern.length() - 1));
        } else if (pattern.startsWith("*")) {
            return className.endsWith(pattern.substring(1));
        } else if (pattern.contains("*")) {
            String regex = pattern.replace(".", "\\.").replace("*", ".*");
            return className.matches(regex);
        } else {
            return className.equals(pattern);
        }
    }

    /**
     * Identify missing test cases for a method.
     */
    private List<String> identifyMissingCases(IMethodCoverage methodCoverage) {
        List<String> missingCases = new ArrayList<>();

        // Check for uncovered branches
        int missedBranches = methodCoverage.getBranchCounter().getMissedCount();
        if (missedBranches > 0) {
            missingCases.add("Missing " + missedBranches + " branch conditions");
        }

        // Analyze lines with missing coverage
        for (int lineNumber = methodCoverage.getFirstLine(); lineNumber <= methodCoverage.getLastLine(); lineNumber++) {
            ILine line = methodCoverage.getLine(lineNumber);

            // Check if this line has branches and some are not covered
            if (line.getBranchCounter().getTotalCount() > 0 &&
                    line.getBranchCounter().getCoveredCount() < line.getBranchCounter().getTotalCount()) {
                missingCases.add("Line " + lineNumber + " has missing branch coverage");
            }

            // Check if line is not covered at all
            if (line.getInstructionCounter().getCoveredCount() == 0 &&
                    line.getInstructionCounter().getTotalCount() > 0) {
                missingCases.add("Line " + lineNumber + " is not covered");
            }
        }

        return missingCases;
    }

    /**
     * Get source code for a class.
     */
    private String getSourceCode(String className) {
        // Convert fully qualified class name to file path
        String relativePath = className.replace('.', '/') + ".java";
        String sourceDir = config.get("coverage").get("sourceDir").asText();
        Path sourcePath = Paths.get(projectRoot, sourceDir, relativePath);

        try {
            if (Files.exists(sourcePath)) {
                return Files.readString(sourcePath);
            }

            // Try to find the file by scanning the source directory
            Path basePath = Paths.get(projectRoot, sourceDir);
            if (Files.exists(basePath)) {
                return findSourceFile(basePath, className);
            }
        } catch (IOException e) {
            System.err.println("Error reading source code for: " + className);
        }

        return "";
    }

    /**
     * Find a source file by searching through directories.
     */
    private String findSourceFile(Path baseDir, String className) throws IOException {
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);

        // Look for files with matching name
        List<Path> candidates = Files.walk(baseDir)
                .filter(path -> path.getFileName().toString().equals(simpleClassName + ".java"))
                .collect(Collectors.toList());

        for (Path candidate : candidates) {
            String content = Files.readString(candidate);

            // Check if the file contains the right package declaration
            String packageName = className.substring(0, className.lastIndexOf('.'));
            if (content.contains("package " + packageName)) {
                return content;
            }
        }

        return "";
    }

    /**
     * Generate a test using AI.
     */
    private String generateTestWithAI(String className, CoverageGap gap, String sourceCode) {
        String aiProvider = config.get("aiProvider").get("type").asText("openai");

        try {
            // Extract the method code
            String methodCode = extractMethodCode(sourceCode, gap);
            if (methodCode.isEmpty()) {
                methodCode = "// Method code not found, using whole class\n" + sourceCode;
            }

            // Create the prompt
            String prompt = createPrompt(className, gap, methodCode);

            // Generate test using the configured AI provider
            if ("openai".equalsIgnoreCase(aiProvider)) {
                return generateWithOpenAI(prompt);
            } else if ("bedrock".equalsIgnoreCase(aiProvider)) {
                return generateWithBedrock(prompt);
            } else {
                System.err.println("Unknown AI provider: " + aiProvider);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error generating test with AI: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract method code from source code.
     */
    private String extractMethodCode(String sourceCode, CoverageGap gap) {
        // This is a simplified extraction - in a real scenario, you'd want to use
        // a more robust parser like JavaParser

        // Try to find the method
        String methodPattern = "\\s*(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+" +
                Pattern.quote(gap.methodName) + "\\s*\\([^\\)]*\\)\\s*(\\{|throws)";

        Pattern pattern = Pattern.compile(methodPattern);
        Matcher matcher = pattern.matcher(sourceCode);

        if (matcher.find()) {
            int startPos = matcher.start();

            // Now find the closing brace for this method
            int braceCount = 0;
            int endPos = startPos;
            boolean inMethod = false;

            for (int i = startPos; i < sourceCode.length(); i++) {
                char c = sourceCode.charAt(i);

                if (c == '{') {
                    braceCount++;
                    inMethod = true;
                } else if (c == '}') {
                    braceCount--;
                    if (inMethod && braceCount == 0) {
                        endPos = i + 1;
                        break;
                    }
                }
            }

            if (endPos > startPos) {
                return sourceCode.substring(startPos, endPos);
            }
        }

        return "";
    }

    /**
     * Create a prompt for the AI.
     */
    private String createPrompt(String className, CoverageGap gap, String methodCode) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate a JUnit 5 test for the following Java method: ")
                .append(gap.methodName).append(" in class ").append(className).append("\n\n");

        prompt.append("Method code:\n```java\n")
                .append(methodCode)
                .append("\n```\n\n");

        prompt.append("The test should focus on these missing coverage cases:\n");
        for (String missingCase : gap.missingCases) {
            prompt.append("- ").append(missingCase).append("\n");
        }

        prompt.append("\nCurrent instruction coverage: ").append(String.format("%.1f%%", gap.instructionCoverage));
        prompt.append("\nCurrent branch coverage: ").append(String.format("%.1f%%", gap.branchCoverage));

        prompt.append("\n\nGenerate a complete JUnit 5 test class that includes:")
                .append("\n1. Necessary imports")
                .append("\n2. Proper setup with mocks using Mockito where needed")
                .append("\n3. Test methods that specifically address the missing coverage cases")
                .append("\n4. Assertions to verify expected behavior")
                .append("\n\nThe test class should be named ").append(className.substring(className.lastIndexOf('.') + 1))
                .append("_").append(gap.methodName).append("Test");

        prompt.append("\n\nPlease provide only the code without explanations, starting with package declaration.");

        return prompt.toString();
    }

    /**
     * Generate test using OpenAI.
     */
    private String generateWithOpenAI(String prompt) {
        try {
            // Get configuration values
            String apiKey = config.get("aiProvider").get("apiKey").asText();
            String model = config.get("aiProvider").get("model").asText("gpt-4");

            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("OpenAI API key not properly configured in testgen.json");
                return null;
            }

            // Format the request body for OpenAI API
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4000);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.set("messages", messages);

            String requestBodyString = objectMapper.writeValueAsString(requestBody);

            // Create and send the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                    .build();

            System.out.println("Sending request to OpenAI API...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check response
            if (response.statusCode() != 200) {
                System.err.println("Error from OpenAI API: " + response.statusCode());
                System.err.println(response.body());
                return null;
            }

            System.out.println("Received response from OpenAI API!");

            // Parse the response to extract the generated test code
            return extractCodeFromOpenAIResponse(response.body());
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate test using AWS Bedrock.
     */
    private String generateWithBedrock(String prompt) {
        try {
            // Get configuration values
            String awsAccessKeyId = config.get("aiProvider").get("awsAccessKeyId").asText();
            String awsSecretKey = config.get("aiProvider").get("awsSecretKey").asText();
            String region = config.get("aiProvider").get("region").asText("us-east-1");
            String model = config.get("aiProvider").get("model").asText("anthropic.claude-3-sonnet-20240229-v1:0");

            if (awsAccessKeyId == null || awsAccessKeyId.isEmpty() || awsSecretKey == null || awsSecretKey.isEmpty()) {
                System.err.println("AWS credentials not properly configured in testgen.json");
                return null;
            }

            // Create the request
            String runtimeService = "bedrock-runtime"; // The actual service endpoint
            String signingService = "bedrock";         // The service name for signing
            String host = runtimeService + "." + region + ".amazonaws.com";
            String endpoint = "https://" + host;
            String uri = "/model/" + model + "/invoke";

            // Format the request body for Claude model
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 4000);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.set("messages", messages);

            String requestBodyString = objectMapper.writeValueAsString(requestBody);

            // Get current date and time
            String amzDate = getAmzDate();
            String dateStamp = amzDate.substring(0, 8);

            // Create canonical request
            String canonicalUri = uri;
            String canonicalQueryString = "";
            String contentType = "application/json";
            String canonicalHeaders = "content-type:" + contentType + "\n" +
                    "host:" + host + "\n" +
                    "x-amz-date:" + amzDate + "\n";
            String signedHeaders = "content-type;host;x-amz-date";

            // Create payload hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] requestBodyHash = md.digest(requestBodyString.getBytes(StandardCharsets.UTF_8));
            String payloadHash = bytesToHex(requestBodyHash);

            String canonicalRequest = "POST\n" +
                    canonicalUri + "\n" +
                    canonicalQueryString + "\n" +
                    canonicalHeaders + "\n" +
                    signedHeaders + "\n" +
                    payloadHash;

            // Create string to sign
            String algorithm = "AWS4-HMAC-SHA256";
            String credentialScope = dateStamp + "/" + region + "/" + signingService + "/aws4_request";

            md = MessageDigest.getInstance("SHA-256");
            byte[] canonicalRequestHash = md.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));
            String stringToSign = algorithm + "\n" +
                    amzDate + "\n" +
                    credentialScope + "\n" +
                    bytesToHex(canonicalRequestHash);

            // Calculate signature
            byte[] signingKey = getSignatureKey(awsSecretKey, dateStamp, region, signingService);
            String signature = bytesToHex(hmacSHA256(signingKey, stringToSign));

            // Create authorization header
            String authorization = algorithm + " " +
                    "Credential=" + awsAccessKeyId + "/" + credentialScope + ", " +
                    "SignedHeaders=" + signedHeaders + ", " +
                    "Signature=" + signature;

            // Create and send the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + uri))
                    .header("Content-Type", contentType)
                    .header("X-Amz-Date", amzDate)
                    .header("Authorization", authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                    .build();

            System.out.println("Sending request to Bedrock API...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check response
            if (response.statusCode() != 200) {
                System.err.println("Error from Bedrock API: " + response.statusCode());
                System.err.println(response.body());
                return null;
            }

            System.out.println("Received response from Bedrock API!");

            // Parse the response to extract the generated test code
            return extractCodeFromBedrockResponse(response.body());
        } catch (Exception e) {
            System.err.println("Error calling Bedrock API: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to get the current date in AWS format.
     */
    private String getAmzDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    /**
     * Helper method to convert bytes to hex string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Helper method to compute HMAC-SHA256.
     */
    private byte[] hmacSHA256(byte[] key, String data) throws Exception {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Helper method to derive the signing key.
     */
    private byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(kSecret, dateStamp);
        byte[] kRegion = hmacSHA256(kDate, regionName);
        byte[] kService = hmacSHA256(kRegion, serviceName);
        return hmacSHA256(kService, "aws4_request");
    }

    /**
     * Extract code from OpenAI API response.
     */
    private String extractCodeFromOpenAIResponse(String responseBody) {
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);

            if (responseJson.has("choices") && responseJson.get("choices").isArray() &&
                    responseJson.get("choices").size() > 0) {
                String content = responseJson.get("choices").get(0).get("message").get("content").asText();
                return extractCodeBlock(content);
            }

            // Fallback - return the whole response
            return responseBody;
        } catch (Exception e) {
            System.err.println("Error extracting code from OpenAI response: " + e.getMessage());
            return responseBody;
        }
    }

    /**
     * Extract code from Bedrock API response.
     */
    private String extractCodeFromBedrockResponse(String responseBody) {
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);

            if (responseJson.has("completion")) {
                String content = responseJson.get("completion").asText();
                return extractCodeBlock(content);
            } else if (responseJson.has("content") && responseJson.get("content").isArray()) {
                String content = responseJson.get("content").get(0).get("text").asText();
                return extractCodeBlock(content);
            }

            // Fallback - return the whole response
            return responseBody;
        } catch (Exception e) {
            System.err.println("Error extracting code from Bedrock response: " + e.getMessage());
            return responseBody;
        }
    }

    /**
     * Extract code block from text content.
     */
    private String extractCodeBlock(String content) {
        // Try to find code between ```java and ``` markers
        Pattern pattern = Pattern.compile("```(?:java)?\\r?\\n(.*?)\\r?\\n```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // If no markers found, return the whole content
        return content;
    }

    /**
     * Save the generated test to a file.
     */
    private void saveGeneratedTest(String className, String methodName, String testCode) {
        try {
            String packageName = className.substring(0, className.lastIndexOf('.'));
            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);

            // Create package declaration if missing
            if (!testCode.startsWith("package ")) {
                testCode = "package " + packageName + ".generated;\n\n" + testCode;
            }

            // Output directory
            String outputDir = config.get("output").get("dir").asText("src/test/java/generated");
            String packageDir = packageName.replace('.', '/');
            Path dirPath = Paths.get(projectRoot, outputDir, packageDir, "generated");

            // Create directories if they don't exist
            Files.createDirectories(dirPath);

            // Create the test file
            String testFileName = simpleClassName + "_" + methodName + "Test.java";
            Path filePath = dirPath.resolve(testFileName);

            // Write the test code to the file
            Files.writeString(filePath, testCode);

            System.out.println("  - Saved test to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving generated test: " + e.getMessage());
        }
    }

    /**
     * Class representing a coverage gap in a method.
     */
    public static class CoverageGap {
        public final String methodName;
        public final String methodDescriptor;
        public final double instructionCoverage;
        public final double branchCoverage;
        public final List<String> missingCases;
        public final int startLine;
        public final int endLine;

        public CoverageGap(String methodName, String methodDescriptor,
                           double instructionCoverage, double branchCoverage,
                           List<String> missingCases, int startLine, int endLine) {
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
            this.instructionCoverage = instructionCoverage;
            this.branchCoverage = branchCoverage;
            this.missingCases = missingCases;
            this.startLine = startLine;
            this.endLine = endLine;
        }

        public String getMethodName() {
            return methodName;
        }

        public List<String> getMissingCases() {
            return missingCases;
        }
    }

    /**
     * Main method to run the test generator.
     */
    public static void main(String[] args) {
        try {
            String configFile = args.length > 0 ? args[0] : null;
            GenericTestGenerator generator = new GenericTestGenerator(configFile);
            generator.run();
        } catch (Exception e) {
            System.err.println("Error running test generator: " + e.getMessage());
            e.printStackTrace();
        }
    }
}