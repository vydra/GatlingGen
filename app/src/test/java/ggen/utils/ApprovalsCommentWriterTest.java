package ggen.utils;

import ggen.utils.ApprovalsCommentWriter.ApprovalsCommentWriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalsCommentWriterTest {
    
    private ApprovalsCommentWriter writer;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        writer = new ApprovalsCommentWriter();
    }
    
    @Test
    void testAddApprovalCommentToTestMethod_HappyPath() throws IOException, ApprovalsCommentWriterException {
        // Create a sample Java test file
        String javaContent = """
            package ggen.test;
            
            import org.junit.jupiter.api.Test;
            import org.approvaltests.Approvals;
            
            class SampleTest {
                
                @Test
                void testGenerateGatlingCodeWithSpecExample() throws IOException {
                    // Test implementation
                    String result = "some result";
                    Approvals.verify(result);
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("SampleTest.java");
        Files.writeString(javaFile, javaContent);
        
        // Create the corresponding approval file
        String approvalContent = """
            http("name of this step")
                .get("/policies")
                .disableUrlEncoding()
                .queryParam("$select","code")
                .queryParam("$filter",Utils.encodeForOdata("code eq 'excludeLate'"))
                .queryParam("$skip","0")
                .queryParam("$top","100")
            .check(status().is(200))""";
        
        Path approvalFile = tempDir.resolve("SampleTest.testGenerateGatlingCodeWithSpecExample.approved.txt");
        Files.writeString(approvalFile, approvalContent);
        
        // Execute the method under test
        writer.addApprovalCommentToTestMethod("SampleTest", "testGenerateGatlingCodeWithSpecExample", javaFile.toString());
        
        // Verify the result
        String modifiedContent = Files.readString(javaFile);
        assertTrue(modifiedContent.contains("http(\"name of this step\")"));
        assertTrue(modifiedContent.contains(".get(\"/policies\")"));
        assertTrue(modifiedContent.contains("Utils.encodeForOdata"));
        assertTrue(modifiedContent.contains("/*"));
        assertTrue(modifiedContent.contains("*/"));
    }
    
    @Test
    void testAddApprovalCommentToTestMethod_ApprovalFileMissing() throws IOException {
        // Create a sample Java test file
        String javaContent = """
            package ggen.test;
            
            import org.junit.jupiter.api.Test;
            
            class SampleTest {
                
                @Test
                void testGenerateGatlingCodeWithSpecExample() throws IOException {
                    // Test implementation
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("SampleTest.java");
        Files.writeString(javaFile, javaContent);
        
        // No approval file created - this should trigger the missing file error
        
        // Execute and verify exception
        ApprovalsCommentWriterException exception = assertThrows(
            ApprovalsCommentWriterException.class,
            () -> writer.addApprovalCommentToTestMethod("SampleTest", "testGenerateGatlingCodeWithSpecExample", javaFile.toString())
        );
        
        assertEquals("Approval file not found for testGenerateGatlingCodeWithSpecExample.", exception.getMessage());
    }
    
    @Test
    void testAddApprovalCommentToTestMethod_ApprovalFileEmpty() throws IOException {
        // Create a sample Java test file
        String javaContent = """
            package ggen.test;
            
            import org.junit.jupiter.api.Test;
            
            class SampleTest {
                
                @Test
                void testGenerateGatlingCodeWithSpecExample() throws IOException {
                    // Test implementation
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("SampleTest.java");
        Files.writeString(javaFile, javaContent);
        
        // Create an empty approval file
        Path approvalFile = tempDir.resolve("SampleTest.testGenerateGatlingCodeWithSpecExample.approved.txt");
        Files.writeString(approvalFile, "");
        
        // Execute and verify exception
        ApprovalsCommentWriterException exception = assertThrows(
            ApprovalsCommentWriterException.class,
            () -> writer.addApprovalCommentToTestMethod("SampleTest", "testGenerateGatlingCodeWithSpecExample", javaFile.toString())
        );
        
        assertEquals("Approval file SampleTest.testGenerateGatlingCodeWithSpecExample.approved.txt is empty.", exception.getMessage());
    }
    
    @Test
    void testAddApprovalCommentToTestMethod_JavaFileNotFound() {
        // Execute and verify exception for non-existent Java file
        IOException exception = assertThrows(
            IOException.class,
            () -> writer.addApprovalCommentToTestMethod("SampleTest", "testMethod", "/non/existent/file.java")
        );
        
        assertTrue(exception.getMessage().contains("Java file not found"));
    }
    
    @Test
    void testAddApprovalCommentToTestMethod_TestMethodNotFound() throws IOException {
        // Create a sample Java test file without the target method
        String javaContent = """
            package ggen.test;
            
            import org.junit.jupiter.api.Test;
            
            class SampleTest {
                
                @Test
                void someOtherTestMethod() throws IOException {
                    // Different test method
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("SampleTest.java");
        Files.writeString(javaFile, javaContent);
        
        // Create the approval file
        Path approvalFile = tempDir.resolve("SampleTest.testGenerateGatlingCodeWithSpecExample.approved.txt");
        Files.writeString(approvalFile, "some content");
        
        // Execute and verify exception
        ApprovalsCommentWriterException exception = assertThrows(
            ApprovalsCommentWriterException.class,
            () -> writer.addApprovalCommentToTestMethod("SampleTest", "testGenerateGatlingCodeWithSpecExample", javaFile.toString())
        );
        
        assertTrue(exception.getMessage().contains("Test method 'testGenerateGatlingCodeWithSpecExample' not found"));
    }
}
