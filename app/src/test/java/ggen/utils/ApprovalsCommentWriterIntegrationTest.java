package ggen.utils;

import ggen.utils.ApprovalsCommentWriter.ApprovalsCommentWriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that ApprovalsCommentWriter correctly places
 * approval comments inside test methods, after the Approvals.verify() call,
 * as specified in specs/approvals_improve.spec.md
 */
class ApprovalsCommentWriterIntegrationTest {
    
    private ApprovalsCommentWriter writer;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        writer = new ApprovalsCommentWriter();
    }
    
    @Test
    void testCommentPlacementFollowsSpecification() throws IOException, ApprovalsCommentWriterException {
        // Create a test Java file that matches the spec example
        String javaContent = """
            package ggen.test;
            
            import org.junit.jupiter.api.Test;
            import org.approvaltests.Approvals;
            import java.io.IOException;
            
            class TestClass {
                
                private String normalizeLineEndings(String text) {
                    return text.replace("\\r\\n", "\\n");
                }
                
                @Test
                void testGenerateGatlingCodeWithSpecExample() throws IOException {
                    CLI cli = new CLI();
                    String filename = "src/test/resources/get_policies.http";
                    
                    String result = cli.generateGatlingCodeFromFile(filename);
                    
                    Approvals.verify(normalizeLineEndings(result));
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("TestClass.java");
        Files.writeString(javaFile, javaContent);
        
        // Create the approval file with the expected content from the spec
        String approvalContent = """
            http("name of this step")
                .get("/policies")
                .disableUrlEncoding()
                .queryParam("$select","code")
                .queryParam("$filter",Utils.encodeForOdata("code eq 'excludeLate'"))
                .queryParam("$skip","0")
                .queryParam("$top","100")
            .check(status().is(200))""";
        
        Path approvalFile = tempDir.resolve("TestClass.testGenerateGatlingCodeWithSpecExample.approved.txt");
        Files.writeString(approvalFile, approvalContent);
        
        // Execute the method under test
        writer.addApprovalCommentToTestMethod("TestClass", "testGenerateGatlingCodeWithSpecExample", javaFile.toString());
        
        // Verify the result matches the specification
        String modifiedContent = Files.readString(javaFile);

        System.out.println("Modified content:");
        System.out.println(modifiedContent);

        // Verify the comment is placed correctly according to the spec
        assertTrue(modifiedContent.contains("Approvals.verify(normalizeLineEndings(result));"),
            "The Approvals.verify call should still be present");

        // Verify the comment appears after the Approvals.verify call
        int approvalsIndex = modifiedContent.indexOf("Approvals.verify(normalizeLineEndings(result));");
        int commentIndex = modifiedContent.indexOf("/**");

        System.out.println("Approvals.verify position: " + approvalsIndex);
        System.out.println("Comment position: " + commentIndex);

        assertTrue(commentIndex > approvalsIndex,
            "The approval comment should appear after the Approvals.verify() call");

        // Verify the comment contains the expected content
        assertTrue(modifiedContent.contains("http(\"name of this step\")"),
            "Comment should contain the approval content");
        assertTrue(modifiedContent.contains(".get(\"/policies\")"),
            "Comment should contain the GET request");
        assertTrue(modifiedContent.contains("Utils.encodeForOdata"),
            "Comment should contain the OData encoding");

        // Verify the comment is properly formatted as a block comment
        assertTrue(modifiedContent.contains("/**"), "Comment should start with /**");
        assertTrue(modifiedContent.contains("*/"), "Comment should end with */");

        // Verify the comment is inside the method body, not as a method-level comment
        String methodSignature = "void testGenerateGatlingCodeWithSpecExample() throws IOException {";
        int methodStart = modifiedContent.indexOf(methodSignature);
        int methodEnd = modifiedContent.indexOf("}", methodStart);

        assertTrue(methodStart < commentIndex && commentIndex < methodEnd,
            "The comment should be inside the method body, not as a method-level comment");
    }
    
    @Test
    void testDemonstrateCorrectCommentPlacement() throws IOException, ApprovalsCommentWriterException {
        // This test demonstrates that the corrected implementation places comments correctly
        // according to the specification in specs/approvals_improve.spec.md

        // Create a test file that exactly matches the spec example
        String javaContent = """
            package ggen.cli;

            import ggen.CLI;
            import org.approvaltests.Approvals;
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.*;
            import java.io.IOException;

            class CLITest {

                private String normalizeLineEndings(String text) {
                    return text.replace("\\r\\n", "\\n");
                }

                @Test
                void testGenerateGatlingCodeWithSpecExample() throws IOException {
                    CLI cli = new CLI();
                    String filename = "src/test/resources/get_policies.http";

                    String result = cli.generateGatlingCodeFromFile(filename);

                    Approvals.verify(normalizeLineEndings(result));
                }
            }
            """;

        Path testFile = tempDir.resolve("CLITest.java");
        Files.writeString(testFile, javaContent);

        // Create the approval file with the exact content from the spec
        String approvalContent = """
            http("name of this step")
                .get("/policies")
                .disableUrlEncoding()
                .queryParam("$select","code")
                .queryParam("$filter",Utils.encodeForOdata("code eq 'excludeLate'"))
                .queryParam("$skip","0")
                .queryParam("$top","100")
            .check(status().is(200))""";

        Path approvalFile = tempDir.resolve("CLITest.testGenerateGatlingCodeWithSpecExample.approved.txt");
        Files.writeString(approvalFile, approvalContent);

        // Apply the corrected implementation
        writer.addApprovalCommentToTestMethod("CLITest", "testGenerateGatlingCodeWithSpecExample", testFile.toString());

        // Verify the result matches the specification exactly
        String result = Files.readString(testFile);

        System.out.println("RESULT AFTER APPLYING CORRECTED IMPLEMENTATION:");
        System.out.println(result);

        // Verify the comment is placed correctly according to the spec
        assertTrue(result.contains("Approvals.verify(normalizeLineEndings(result));"), "Should contain Approvals.verify call");

        int approvalsIndex = result.indexOf("Approvals.verify(normalizeLineEndings(result));");
        int commentIndex = result.indexOf("/**");

        assertTrue(approvalsIndex > 0, "Should find Approvals.verify call");
        assertTrue(commentIndex > 0, "Should find comment starting with /**");
        assertTrue(commentIndex > approvalsIndex, "Comment should appear AFTER Approvals.verify call");

        // Verify the comment contains the expected content from the spec
        assertTrue(result.contains("http(\"name of this step\")"), "Should contain expected Gatling code");
        assertTrue(result.contains(".get(\"/policies\")"), "Should contain GET request");
        assertTrue(result.contains("Utils.encodeForOdata"), "Should contain OData encoding");

        // Verify the comment is inside the method body
        int methodStart = result.indexOf("void testGenerateGatlingCodeWithSpecExample() throws IOException {");
        int methodEnd = result.lastIndexOf("}");
        assertTrue(methodStart < commentIndex && commentIndex < methodEnd,
            "Comment should be inside the method body");

        System.out.println("✓ CORRECTED IMPLEMENTATION WORKS PERFECTLY!");
        System.out.println("✓ Comment placed after Approvals.verify() call as specified");
        System.out.println("✓ Comment contains expected approval content");
        System.out.println("✓ Comment is properly formatted as /** block comment */");
    }

    @Test
    void testCommentIsPlacedAtEndOfMethodBody() throws IOException, ApprovalsCommentWriterException {
        // Create a test with multiple statements to ensure comment goes at the end
        String javaContent = """
            package ggen.test;
            
            import org.junit.jupiter.api.Test;
            import org.approvaltests.Approvals;
            
            class TestClass {
                
                @Test
                void testMethod() {
                    String result = "test";
                    Approvals.verify(result);
                    // This comment should remain
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("TestClass.java");
        Files.writeString(javaFile, javaContent);
        
        Path approvalFile = tempDir.resolve("TestClass.testMethod.approved.txt");
        Files.writeString(approvalFile, "expected output");
        
        writer.addApprovalCommentToTestMethod("TestClass", "testMethod", javaFile.toString());
        
        String modifiedContent = Files.readString(javaFile);
        
        // Verify the approval comment is at the end, after the existing comment
        int existingCommentIndex = modifiedContent.indexOf("// This comment should remain");
        int approvalCommentIndex = modifiedContent.indexOf("/**");
        
        assertTrue(approvalCommentIndex > existingCommentIndex, 
            "Approval comment should be placed at the end of the method body");
    }
}
