package ggen;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CLITest {
    
    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }
    
    @Test
    void testGenerateGatlingCodeWithSpecExample() {
        CLI cli = new CLI();
        String input = "GET policies?$select=code&$filter=code%20eq%20'excludeLate'&$skip=0&$top=100";
        
        String result = cli.generateGatlingCode(input);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeSimpleGet() {
        CLI cli = new CLI();
        String input = "GET /users";
        
        String result = cli.generateGatlingCode(input);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeWithQueryParams() {
        CLI cli = new CLI();
        String input = "GET /users?name=john&age=30";
        
        String result = cli.generateGatlingCode(input);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodePostRequest() {
        CLI cli = new CLI();
        String input = "POST /api/users?format=json";
        
        String result = cli.generateGatlingCode(input);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGenerateGatlingCodeWithODataFilter() {
        CLI cli = new CLI();
        String input = "GET /products?$filter=price%20gt%2020";
        
        String result = cli.generateGatlingCode(input);
        
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
        
        StringBuilder results = new StringBuilder();
        results.append("GET:\n");
        results.append(cli.generateGatlingCode("GET /users")).append("\n\n");
        
        results.append("POST:\n");
        results.append(cli.generateGatlingCode("POST /users")).append("\n\n");
        
        results.append("PUT:\n");
        results.append(cli.generateGatlingCode("PUT /users/123")).append("\n\n");
        
        results.append("DELETE:\n");
        results.append(cli.generateGatlingCode("DELETE /users/123"));
        
        Approvals.verify(normalizeLineEndings(results.toString()));
    }
    
    @Test
    void testGenerateGatlingCodeWithPathWithoutLeadingSlash() {
        CLI cli = new CLI();
        String input = "GET users?id=123";
        
        String result = cli.generateGatlingCode(input);
        
        Approvals.verify(normalizeLineEndings(result));
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
