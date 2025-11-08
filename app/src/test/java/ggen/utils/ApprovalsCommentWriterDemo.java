package ggen.utils;

import ggen.utils.ApprovalsCommentWriter.ApprovalsCommentWriterException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Demonstration class showing how to use ApprovalsCommentWriter to add
 * approval test content as comments to existing test methods.
 * 
 * This class provides examples of how to integrate the ApprovalsCommentWriter
 * into your development workflow.
 */
public class ApprovalsCommentWriterDemo {
    
    /**
     * Example usage of ApprovalsCommentWriter.
     * 
     * This method demonstrates how to add approval comments to the existing
     * CLITest.java file for the testGenerateGatlingCodeWithSpecExample method.
     */
    public static void main(String[] args) {
        ApprovalsCommentWriter writer = new ApprovalsCommentWriter();
        
        try {
            // Example 1: Add comment to CLITest method
            String cliTestPath = "app/src/test/java/ggen/cli/CLITest.java";
            Path absolutePath = Paths.get(System.getProperty("user.dir"), cliTestPath);
            
            System.out.println("Adding approval comment to CLITest.testGenerateGatlingCodeWithSpecExample...");
            writer.addApprovalCommentToTestMethod(
                "CLITest", 
                "testGenerateGatlingCodeWithSpecExample", 
                absolutePath.toString()
            );
            System.out.println("✓ Successfully added comment to CLITest");
            
            // Example 2: Add comment to GatlingCodeGeneratorTest method
            String generatorTestPath = "app/src/test/java/ggen/GatlingCodeGeneratorTest.java";
            Path generatorAbsolutePath = Paths.get(System.getProperty("user.dir"), generatorTestPath);
            
            System.out.println("Adding approval comment to GatlingCodeGeneratorTest.testGenerateCompleteExampleFromSpec...");
            writer.addApprovalCommentToTestMethod(
                "GatlingCodeGeneratorTest", 
                "testGenerateCompleteExampleFromSpec", 
                generatorAbsolutePath.toString()
            );
            System.out.println("✓ Successfully added comment to GatlingCodeGeneratorTest");
            
            System.out.println("\nDemo completed successfully!");
            System.out.println("Check the modified test files to see the approval comments.");
            
        } catch (ApprovalsCommentWriterException e) {
            System.err.println("ApprovalsCommentWriter error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Utility method to add approval comments to multiple test methods in a batch.
     * 
     * @param testFilePath the path to the test file
     * @param testClassName the name of the test class
     * @param methodNames array of method names to process
     */
    public static void addCommentsToMultipleMethods(String testFilePath, String testClassName, String[] methodNames) {
        ApprovalsCommentWriter writer = new ApprovalsCommentWriter();
        
        for (String methodName : methodNames) {
            try {
                System.out.println("Processing method: " + methodName);
                writer.addApprovalCommentToTestMethod(testClassName, methodName, testFilePath);
                System.out.println("✓ Successfully processed " + methodName);
            } catch (ApprovalsCommentWriterException e) {
                System.err.println("✗ Failed to process " + methodName + ": " + e.getMessage());
            } catch (IOException e) {
                System.err.println("✗ IO error processing " + methodName + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Example of batch processing multiple methods.
     */
    public static void batchProcessExample() {
        String cliTestPath = Paths.get(System.getProperty("user.dir"), 
            "app/src/test/java/ggen/cli/CLITest.java").toString();
        
        String[] methodsToProcess = {
            "testGenerateGatlingCodeWithSpecExample",
            "testGenerateGatlingCodeSimpleGet",
            "testGenerateGatlingCodeWithQueryParams",
            "testGenerateGatlingCodePostRequest"
        };
        
        System.out.println("Batch processing CLITest methods...");
        addCommentsToMultipleMethods(cliTestPath, "CLITest", methodsToProcess);
        System.out.println("Batch processing completed.");
    }
}
