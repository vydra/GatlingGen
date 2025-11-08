package ggen.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Utility class for adding approval test content as comments to JUnit test methods.
 *
 * This class reads the content of ApprovalTests .approved.txt files and inserts
 * them as block comments into the corresponding test methods. This helps developers
 * see the expected output directly in the test code.
 *
 * @see specs/approvals_improve.spec.md
 */
public class ApprovalsCommentWriter {

    private static final String APPROVED_FILE_SUFFIX = ".approved.txt";

    /**
     * Adds the content of an approval file as a block comment to a test method.
     *
     * @param testClassName the name of the test class (e.g., "CLITest")
     * @param testMethodName the name of the test method (e.g., "testGenerateGatlingCodeWithSpecExample")
     * @param javaFilePath the path to the Java test file to modify
     * @throws ApprovalsCommentWriterException if the approval file is missing, empty, or cannot be processed
     * @throws IOException if there are issues reading or writing files
     */
    public void addApprovalCommentToTestMethod(String testClassName, String testMethodName, String javaFilePath)
            throws ApprovalsCommentWriterException, IOException {

        // First, validate that the Java file exists and can be parsed
        JavaParser javaParser = new JavaParser();
        Path filePath = Paths.get(javaFilePath);

        if (!Files.exists(filePath)) {
            throw new IOException("Java file not found: " + javaFilePath);
        }

        Optional<CompilationUnit> parseResult = javaParser.parse(filePath).getResult();
        if (parseResult.isEmpty()) {
            throw new IOException("Failed to parse Java file: " + javaFilePath);
        }

        CompilationUnit compilationUnit = parseResult.get();

        // Then, find and read the approval file
        String approvalContent = readApprovalFile(testClassName, testMethodName, javaFilePath);

        // Find the test method and add the comment
        boolean methodFound = addCommentToMethod(compilationUnit, testMethodName, approvalContent);

        if (!methodFound) {
            throw new ApprovalsCommentWriterException("Test method '" + testMethodName + "' not found in " + javaFilePath);
        }

        // Write the modified Java file back
        Files.writeString(filePath, compilationUnit.toString());
    }

    /**
     * Reads the content of an approval file for a given test class and method.
     *
     * @param testClassName the name of the test class
     * @param testMethodName the name of the test method
     * @param javaFilePath the path to the Java test file (used to determine the approval file location)
     * @return the content of the approval file
     * @throws ApprovalsCommentWriterException if the approval file is missing or empty
     * @throws IOException if there are issues reading the file
     */
    private String readApprovalFile(String testClassName, String testMethodName, String javaFilePath)
            throws ApprovalsCommentWriterException, IOException {

        // Construct the approval file path
        Path javaPath = Paths.get(javaFilePath);
        Path parentDir = javaPath.getParent();
        String approvalFileName = testClassName + "." + testMethodName + APPROVED_FILE_SUFFIX;
        Path approvalFilePath = parentDir.resolve(approvalFileName);

        // Check if approval file exists
        if (!Files.exists(approvalFilePath)) {
            throw new ApprovalsCommentWriterException("Approval file not found for " + testMethodName + ".");
        }

        // Read the approval file content
        String content = Files.readString(approvalFilePath).trim();

        // Check if approval file is empty
        if (content.isEmpty()) {
            throw new ApprovalsCommentWriterException("Approval file " + approvalFileName + " is empty.");
        }

        return content;
    }

    /**
     * Adds a block comment containing the approval content to the specified test method.
     *
     * @param compilationUnit the parsed Java compilation unit
     * @param testMethodName the name of the test method to modify
     * @param approvalContent the content to add as a comment
     * @return true if the method was found and modified, false otherwise
     */
    private boolean addCommentToMethod(CompilationUnit compilationUnit, String testMethodName, String approvalContent) {
        MethodVisitor methodVisitor = new MethodVisitor(testMethodName, approvalContent);
        methodVisitor.visit(compilationUnit, null);
        return methodVisitor.isMethodFound();
    }

    /**
     * Formats the approval content as a proper Java block comment.
     *
     * @param content the raw approval content
     * @return the formatted block comment content
     */
    private String formatAsBlockComment(String content) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = content.split("\n");

        for (String line : lines) {
            formatted.append(" * ").append(line).append("\n");
        }

        return formatted.toString().trim();
    }

    /**
     * Custom exception for ApprovalsCommentWriter specific errors.
     */
    public static class ApprovalsCommentWriterException extends Exception {
        public ApprovalsCommentWriterException(String message) {
            super(message);
        }

        public ApprovalsCommentWriterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Visitor class to find and modify specific test methods.
     */
    private class MethodVisitor extends VoidVisitorAdapter<Void> {
        private final String targetMethodName;
        private final String approvalContent;
        private boolean methodFound = false;

        public MethodVisitor(String targetMethodName, String approvalContent) {
            this.targetMethodName = targetMethodName;
            this.approvalContent = approvalContent;
        }

        @Override
        public void visit(MethodDeclaration method, Void arg) {
            super.visit(method, arg);

            if (method.getNameAsString().equals(targetMethodName)) {
                methodFound = true;

                // Remove any existing method-level comments that might have been added incorrectly
                if (method.getComment().isPresent()) {
                    method.removeComment();
                }

                // Get the method body
                if (method.getBody().isPresent()) {
                    BlockStmt body = method.getBody().get();

                    // Remove any existing approval comments from the method body
                    body.getAllContainedComments().stream()
                        .filter(comment -> comment instanceof BlockComment)
                        .filter(comment -> comment.getContent().contains("http(\"name of this step\")"))
                        .forEach(comment -> comment.remove());

                    // Create and add the new block comment at the end of the method body
                    String formattedComment = formatAsBlockComment(approvalContent);
                    BlockComment blockComment = new BlockComment("*\n" + formattedComment + "\n         ");

                    // Add an empty statement with the comment at the end of the method body
                    // This ensures the comment appears as the last thing in the method
                    EmptyStmt emptyStmt = new EmptyStmt();
                    emptyStmt.setComment(blockComment);
                    body.addStatement(emptyStmt);
                }
            }
        }

        public boolean isMethodFound() {
            return methodFound;
        }
    }
}
