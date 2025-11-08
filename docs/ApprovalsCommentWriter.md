# ApprovalsCommentWriter

The `ApprovalsCommentWriter` is a utility class that automatically adds the content of ApprovalTests `.approved.txt` files as block comments to the corresponding JUnit test methods. This helps developers see the expected output directly in the test code, improving code readability and understanding.

## Overview

When using ApprovalTests, the expected output is stored in separate `.approved.txt` files. While this is great for version control and test maintenance, it can be difficult for developers to quickly see what the expected output should be when reading the test code. The `ApprovalsCommentWriter` solves this by automatically inserting the approval file content as Java block comments in the test methods.

## Features

- **Automatic Comment Generation**: Reads approval files and formats them as proper Java block comments
- **Error Handling**: Provides clear error messages for missing or empty approval files
- **JavaParser Integration**: Uses JavaParser to safely modify Java source files
- **Batch Processing**: Supports processing multiple test methods at once
- **Validation**: Validates both Java files and approval files before processing

## Usage

### Basic Usage

```java
ApprovalsCommentWriter writer = new ApprovalsCommentWriter();

try {
    writer.addApprovalCommentToTestMethod(
        "CLITest",                                    // Test class name
        "testGenerateGatlingCodeWithSpecExample",     // Test method name
        "app/src/test/java/ggen/cli/CLITest.java"    // Path to Java file
    );
} catch (ApprovalsCommentWriterException | IOException e) {
    System.err.println("Error: " + e.getMessage());
}
```

### Batch Processing

```java
String[] methodNames = {
    "testGenerateGatlingCodeWithSpecExample",
    "testGenerateGatlingCodeSimpleGet",
    "testGenerateGatlingCodeWithQueryParams"
};

for (String methodName : methodNames) {
    try {
        writer.addApprovalCommentToTestMethod("CLITest", methodName, testFilePath);
    } catch (Exception e) {
        System.err.println("Failed to process " + methodName + ": " + e.getMessage());
    }
}
```

## File Naming Convention

The class follows the standard ApprovalTests naming convention:
- Approval files must be named: `{ClassName}.{methodName}.approved.txt`
- Example: `CLITest.testGenerateGatlingCodeWithSpecExample.approved.txt`

## Input and Output Example

### Before (Test Method)
```java
@Test
void testGenerateGatlingCodeWithSpecExample() throws IOException {
    CLI cli = new CLI();
    String filename = "src/test/resources/get_policies.http";
    
    String result = cli.generateGatlingCodeFromFile(filename);
    
    Approvals.verify(normalizeLineEndings(result));
}
```

### Approval File Content
```
http("name of this step")
    .get("/policies")
    .disableUrlEncoding()
    .queryParam("$select","code")
    .queryParam("$filter",Utils.encodeForOdata("code eq 'excludeLate'"))
    .queryParam("$skip","0")
    .queryParam("$top","100")
.check(status().is(200))
```

### After (Test Method with Comment)
```java
@Test
void testGenerateGatlingCodeWithSpecExample() throws IOException {
    CLI cli = new CLI();
    String filename = "src/test/resources/get_policies.http";
    
    String result = cli.generateGatlingCodeFromFile(filename);
    
    Approvals.verify(normalizeLineEndings(result));
    /**
     * http("name of this step")
     *     .get("/policies")
     *     .disableUrlEncoding()
     *     .queryParam("$select","code")
     *     .queryParam("$filter",Utils.encodeForOdata("code eq 'excludeLate'"))
     *     .queryParam("$skip","0")
     *     .queryParam("$top","100")
     * .check(status().is(200))
     */
}
```

## Error Handling

The class provides specific error messages for different failure scenarios:

### Missing Approval File
```
ApprovalsCommentWriterException: Approval file not found for testMethodName.
```

### Empty Approval File
```
ApprovalsCommentWriterException: Approval file ClassName.methodName.approved.txt is empty.
```

### Missing Java File
```
IOException: Java file not found: /path/to/file.java
```

### Method Not Found
```
ApprovalsCommentWriterException: Test method 'methodName' not found in /path/to/file.java
```

## Dependencies

- **JavaParser**: Used for parsing and modifying Java source files
- **JUnit 5**: For testing the implementation
- **Java 21**: Required for the project

## Integration with Build Process

You can integrate the `ApprovalsCommentWriter` into your build process or IDE workflow:

1. **Gradle Task**: Create a custom Gradle task to run the comment writer
2. **IDE Plugin**: Integrate with your IDE to run on demand
3. **Git Hook**: Run as a pre-commit hook to ensure comments are up-to-date
4. **CI/CD**: Include in your continuous integration pipeline

## Best Practices

1. **Version Control**: Commit both the approval files and the updated test files
2. **Regular Updates**: Re-run the comment writer when approval files change
3. **Code Reviews**: Review the generated comments as part of your code review process
4. **Backup**: Always backup your test files before running batch operations

## Limitations

- Only works with ApprovalTests that use the standard naming convention
- Requires valid Java syntax in the target files
- Comments are replaced each time the tool runs (no merging of existing comments)
- Only supports block comments (not line comments)
