package ggen;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

class CLITest {
    
    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }
    
    @Test
    void testGenerateGatlingCodeWithSpecExample() throws IOException {
        CLI cli = new CLI();
        String filename = "src/test/resources/get_policies.http";
        
        String result = cli.generateGatlingCodeFromFile(filename);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeSimpleGet() throws IOException {
        CLI cli = new CLI();
        String filename = "src/test/resources/simple_get.http";
        
        String result = cli.generateGatlingCodeFromFile(filename);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeWithQueryParams() throws IOException {
        CLI cli = new CLI();
        String filename = "src/test/resources/get_with_query_params.http";
        
        String result = cli.generateGatlingCodeFromFile(filename);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodePostRequest() throws IOException {
        CLI cli = new CLI();
        String filename = "src/test/resources/post_request.http";
        
        String result = cli.generateGatlingCodeFromFile(filename);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeWithODataFilter() throws IOException {
        CLI cli = new CLI();
        String filename = "src/test/resources/odata_filter.http";
        
        String result = cli.generateGatlingCodeFromFile(filename);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeWithComplexODataQuery() {
        CLI cli = new CLI();
        String input = "GET /odata/Products?$filter=Price%20gt%2020%20and%20Price%20lt%20100&$orderby=Name&$top=10";
        
        String result = cli.generateGatlingCode(input);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeWithUrlEncodedSpecialChars() {
        CLI cli = new CLI();
        String input = "GET /search?q=hello%20world&email=user%40example.com";
        
        String result = cli.generateGatlingCode(input);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeWithDifferentHttpMethods() {
        CLI cli = new CLI();

        String results = "GET:\n" +
                         cli.generateGatlingCode("GET /users") + "\n\n" +
                         "POST:\n" +
                         cli.generateGatlingCode("POST /users") + "\n\n" +
                         "PUT:\n" +
                         cli.generateGatlingCode("PUT /users/123") + "\n\n" +
                         "DELETE:\n" +
                         cli.generateGatlingCode("DELETE /users/123");
        
        Approvals.verify(normalizeLineEndings(results));
    }
    
    @Test
    void testGenerateGatlingCodeWithPathWithoutLeadingSlash() throws IOException {
        CLI cli = new CLI();
        String filename = "src/test/resources/path_without_slash.http";
        
        String result = cli.generateGatlingCodeFromFile(filename);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeFileNotFound() {
        CLI cli = new CLI();
        
        assertThrows(IOException.class, () -> {
            cli.generateGatlingCodeFromFile("nonexistent.http");
        });
    }
    
    @Test
    void testGenerateGatlingCodeEmptyFile() {
        CLI cli = new CLI();
        
        assertThrows(IllegalArgumentException.class, () -> {
            cli.generateGatlingCodeFromFile("src/test/resources/empty_file.http");
        });
    }
    
    @Test
    void testGenerateGatlingCodeInvalidInputThrowsException() {
        CLI cli = new CLI();
        
        assertThrows(IllegalArgumentException.class, () -> {
            cli.generateGatlingCode(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cli.generateGatlingCode("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cli.generateGatlingCode("GETUSERS");
        });
    }
}
