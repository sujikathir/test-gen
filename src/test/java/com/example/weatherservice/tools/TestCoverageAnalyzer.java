package com.example.weatherservice.tools;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Analyzes test coverage and identifies gaps for automated test generation.
 */
public class TestCoverageAnalyzer {

    private final IClassCoverage classCoverage;
    private final Map<String, List<MethodGap>> gapsByClass = new HashMap<>();

    /**
     * Constructor for analyzing a specific class.
     *
     * @param execFile JaCoCo execution data file
     * @param className Class to analyze
     * @throws IOException If the execution data file cannot be read
     */
    public TestCoverageAnalyzer(File execFile, String className) throws IOException {
        ExecFileLoader loader = new ExecFileLoader();
        loader.load(execFile);

        ExecutionDataStore executionData = loader.getExecutionDataStore();
        SessionInfoStore sessionInfo = loader.getSessionInfoStore();

        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

        // Analyze the class file
        analyzer.analyzeAll(new File("build/classes"));

        // Find the class coverage
        IClassCoverage targetClass = null;
        for (IClassCoverage clazz : coverageBuilder.getClasses()) {
            if (clazz.getName().equals(className.replace('.', '/'))) {
                targetClass = clazz;
                break;
            }
        }

        this.classCoverage = targetClass;

        if (targetClass != null) {
            analyzeMethodCoverage(targetClass);
        }
    }

    /**
     * Analyzes method coverage for a class.
     *
     * @param classCoverage The class coverage data
     */
    private void analyzeMethodCoverage(IClassCoverage classCoverage) {
        String className = classCoverage.getName().replace('/', '.');
        List<MethodGap> gaps = new ArrayList<>();

        for (IMethodCoverage methodCoverage : classCoverage.getMethods()) {
            // Skip constructors and static initializers
            if ("<init>".equals(methodCoverage.getName()) || "<clinit>".equals(methodCoverage.getName())) {
                continue;
            }

            // Calculate method coverage
            int coveredInstructions = methodCoverage.getInstructionCounter().getCoveredCount();
            int totalInstructions = methodCoverage.getInstructionCounter().getTotalCount();
            int coveredBranches = methodCoverage.getBranchCounter().getCoveredCount();
            int totalBranches = methodCoverage.getBranchCounter().getTotalCount();

            double instructionCoverage = (double) coveredInstructions / totalInstructions;
            double branchCoverage = totalBranches > 0 ? (double) coveredBranches / totalBranches : 1.0;

            // Identify methods with low coverage
            if (instructionCoverage < 0.8 || branchCoverage < 0.8) {
                gaps.add(new MethodGap(
                        methodCoverage.getName(),
                        methodCoverage.getDesc(),
                        instructionCoverage,
                        branchCoverage,
                        identifyMissingCases(methodCoverage)
                ));
            }
        }

        gapsByClass.put(className, gaps);
    }

    /**
     * Identifies specific missing test cases for a method.
     *
     * @param methodCoverage The method coverage data
     * @return List of missing test case descriptions
     */
    private List<String> identifyMissingCases(IMethodCoverage methodCoverage) {
        List<String> missingCases = new ArrayList<>();

        // Check for uncovered branches
        int missedBranches = methodCoverage.getBranchCounter().getMissedCount();
        if (missedBranches > 0) {
            missingCases.add("Missing " + missedBranches + " branch test cases");
        }

        // Analyze lines with conditional statements
        for (int lineNumber = methodCoverage.getFirstLine(); lineNumber <= methodCoverage.getLastLine(); lineNumber++) {
            ILine line = methodCoverage.getLine(lineNumber);

            // Check if this line has branches and some are not covered
            if (line.getBranchCounter().getTotalCount() > 0 &&
                    line.getBranchCounter().getCoveredCount() < line.getBranchCounter().getTotalCount()) {
                missingCases.add("Line " + lineNumber + ": missing branch coverage");
            }
        }

        return missingCases;
    }

    /**
     * Generates test templates for missing coverage.
     *
     * @return String containing test templates
     */
    public String generateTestTemplates() {
        StringBuilder templates = new StringBuilder();

        for (Map.Entry<String, List<MethodGap>> entry : gapsByClass.entrySet()) {
            String className = entry.getKey();
            List<MethodGap> methodGaps = entry.getValue();

            if (methodGaps.isEmpty()) {
                continue;
            }

            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);

            templates.append("// Test class for ").append(simpleClassName).append("\n");
            templates.append("class ").append(simpleClassName).append("Test {\n\n");

            for (MethodGap gap : methodGaps) {
                templates.append("    /**\n");
                templates.append("     * Test for method: ").append(gap.methodName).append("\n");
                templates.append("     * Current coverage: ").append(Math.round(gap.instructionCoverage * 100)).append("% instructions, ")
                        .append(Math.round(gap.branchCoverage * 100)).append("% branches\n");
                templates.append("     * Missing cases:\n");

                for (String missingCase : gap.missingCases) {
                    templates.append("     * - ").append(missingCase).append("\n");
                }

                templates.append("     */\n");
                templates.append("    @Test\n");
                templates.append("    void test").append(capitalizeFirstLetter(gap.methodName)).append("_MissingCases() {\n");
                templates.append("        // TODO: Implement test for missing cases\n");
                templates.append("        // ...\n");
                templates.append("    }\n\n");
            }

            templates.append("}\n\n");
        }

        return templates.toString();
    }

    private String capitalizeFirstLetter(String input) {
        if (input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Class representing a gap in method test coverage.
     */
    public static class MethodGap {
        private final String methodName;
        private final String methodDesc;
        private final double instructionCoverage;
        private final double branchCoverage;
        private final List<String> missingCases;

        public MethodGap(
                String methodName,
                String methodDesc,
                double instructionCoverage,
                double branchCoverage,
                List<String> missingCases) {
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.instructionCoverage = instructionCoverage;
            this.branchCoverage = branchCoverage;
            this.missingCases = missingCases;
        }
    }

    /**
     * Main method to run the analyzer.
     */
    public static void main(String[] args) {
        try {
            File execFile = new File("build/jacoco/test.exec");
            TestCoverageAnalyzer analyzer = new TestCoverageAnalyzer(
                    execFile,
                    "com.example.weatherservice.service.WeatherAggregatorService"
            );

            String templates = analyzer.generateTestTemplates();
            System.out.println(templates);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}