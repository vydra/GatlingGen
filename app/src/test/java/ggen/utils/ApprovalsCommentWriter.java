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

        // Find the test method and prepare the comment
        MethodVisitor methodVisitor = new MethodVisitor(testMethodName, approvalContent);
        methodVisitor.visit(compilationUnit, null);

        if (!methodVisitor.isMethodFound()) {
            throw new ApprovalsCommentWriterException("Test method '" + testMethodName + "' not found in " + javaFilePath);
        }

        // Get the modified source code
        String modifiedSource = compilationUnit.toString();

        // If we have a formatted comment to add, insert it properly
        if (methodVisitor.getFormattedCommentToAdd() != null) {
            modifiedSource = insertFormattedComment(modifiedSource, testMethodName, methodVisitor.getFormattedCommentToAdd());
        }

        // Write the modified Java file back
        Files.writeString(filePath, modifiedSource);
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
     * Inserts a properly formatted comment block into the source code after the Approvals.verify() call.
     *
     * @param sourceCode the source code to modify
     * @param testMethodName the name of the test method
     * @param formattedComment the properly formatted comment block to insert
     * @return the modified source code with the comment inserted
     */
    private String insertFormattedComment(String sourceCode, String testMethodName, String formattedComment) {
        // Find the method and the Approvals.verify() call
        String methodSignature = "void " + testMethodName + "(";
        int methodStart = sourceCode.indexOf(methodSignature);

        if (methodStart == -1) {
            return sourceCode; // Method not found, return unchanged
        }

        // Find the Approvals.verify() call within this method
        int approvalsCall = sourceCode.indexOf("Approvals.verify(", methodStart);
        if (approvalsCall == -1) {
            return sourceCode; // Approvals.verify not found, return unchanged
        }

        // Find the beginning of the line containing Approvals.verify() to detect indentation
        int lineStart = sourceCode.lastIndexOf("\n", approvalsCall) + 1;
        String approvalsLine = sourceCode.substring(lineStart, sourceCode.indexOf("\n", approvalsCall));

        // Extract the indentation from the Approvals.verify() line
        StringBuilder indentationBuilder = new StringBuilder();
        for (char c : approvalsLine.toCharArray()) {
            if (c == ' ' || c == '\t') {
                indentationBuilder.append(c);
            } else {
                break;
            }
        }
        String actualIndentation = indentationBuilder.toString();

        // Create the properly indented comment block
        String properlyIndentedComment = createIndentedComment(formattedComment, actualIndentation);

        // Find the method's closing brace to place the comment at the very end
        int methodOpenBrace = sourceCode.indexOf("{", methodStart);
        if (methodOpenBrace == -1) {
            return sourceCode; // Opening brace not found, return unchanged
        }

        // Find the matching closing brace for this method
        int braceCount = 1;
        int pos = methodOpenBrace + 1;
        int methodCloseBrace = -1;

        while (pos < sourceCode.length() && braceCount > 0) {
            char c = sourceCode.charAt(pos);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    methodCloseBrace = pos;
                    break;
                }
            }
            pos++;
        }

        if (methodCloseBrace == -1) {
            return sourceCode; // Closing brace not found, return unchanged
        }

        // Find the last newline before the closing brace to insert the comment
        int insertPoint = sourceCode.lastIndexOf("\n", methodCloseBrace);
        if (insertPoint == -1) {
            insertPoint = methodCloseBrace; // No newline found, insert right before the brace
        } else {
            insertPoint++; // Include the newline
        }

        // Insert the properly indented comment at the insertion point
        return sourceCode.substring(0, insertPoint) + properlyIndentedComment + "\n" + sourceCode.substring(insertPoint);
    }

    /**
     * Creates a properly indented comment block using the detected indentation.
     *
     * @param originalComment the original comment content
     * @param indentation the indentation to use
     * @return the properly indented comment block
     */
    private String createIndentedComment(String originalComment, String indentation) {
        // The original comment was created with a default indentation, we need to replace it
        String[] lines = originalComment.split("\n");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i == 0 && line.trim().equals("/**")) {
                // Opening comment line
                result.append(indentation).append("/**");
            } else if (i == lines.length - 1 && line.trim().equals("*/")) {
                // Closing comment line
                result.append(indentation).append(" */");
            } else if (line.trim().startsWith("*")) {
                // Content line - preserve the content but use correct indentation
                String content = line.substring(line.indexOf("*"));
                result.append(indentation).append(" ").append(content);
            } else {
                // Other lines - preserve as is but with correct indentation
                result.append(indentation).append(line.trim());
            }

            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Formats the approval content as a proper Java block comment with proper indentation.
     *
     * @param content the raw approval content
     * @param indentation the indentation string to use for each line
     * @return the formatted block comment content including opening and closing markers
     */
    private String formatAsBlockComment(String content, String indentation) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = content.split("\n");

        // Add opening comment marker with proper indentation
        formatted.append(indentation).append("/**\n");

        // Add each content line with proper indentation
        for (String line : lines) {
            formatted.append(indentation).append(" * ").append(line).append("\n");
        }

        // Add closing comment marker with proper indentation
        formatted.append(indentation).append(" */");

        return formatted.toString();
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
        private String formattedCommentToAdd = null;

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

                    // Detect the indentation level by looking at existing statements
                    String indentation = detectIndentation(body);

                    // Create the properly formatted comment block
                    String formattedComment = formatAsBlockComment(approvalContent, indentation);

                    // Since JavaParser doesn't preserve custom indentation in comments,
                    // we'll modify the source code directly after JavaParser processing
                    // For now, store the formatted comment for post-processing
                    this.formattedCommentToAdd = formattedComment;
                }
            }
        }

        /**
         * Detects the indentation level used in the method body by examining existing statements.
         *
         * @param body the method body to analyze
         * @return the indentation string (spaces or tabs) used in the method
         */
        private String detectIndentation(BlockStmt body) {
            // Default indentation - typically 8 spaces for method body content
            String defaultIndentation = "        ";

            if (body.getStatements().isEmpty()) {
                return defaultIndentation;
            }

            // Get the string representation of the method body to analyze indentation
            String bodyStr = body.toString();
            String[] lines = bodyStr.split("\n");

            // Look for the first non-empty line that contains a statement
            for (String line : lines) {
                if (line.trim().length() > 0 && !line.trim().equals("{") && !line.trim().equals("}")) {
                    // Count leading whitespace
                    int leadingSpaces = 0;
                    for (char c : line.toCharArray()) {
                        if (c == ' ') {
                            leadingSpaces++;
                        } else if (c == '\t') {
                            leadingSpaces += 4; // Treat tab as 4 spaces
                        } else {
                            break;
                        }
                    }

                    // Return the detected indentation
                    StringBuilder indentation = new StringBuilder();
                    for (int i = 0; i < leadingSpaces; i++) {
                        indentation.append(" ");
                    }
                    return indentation.toString();
                }
            }

            return defaultIndentation;
        }

        public boolean isMethodFound() {
            return methodFound;
        }

        public String getFormattedCommentToAdd() {
            return formattedCommentToAdd;
        }
    }
}
