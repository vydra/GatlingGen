package ggen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class HttpCallParserTest {
    
    @Test
    void testParseSimpleGetRequest() {
        HttpCallParser parser = new HttpCallParser("GET /users");
        
        assertEquals("GET", parser.getMethod());
        assertEquals("/users", parser.getPath());
        assertTrue(parser.getQueryParams().isEmpty());
    }
    
    @Test
    void testParseGetRequestWithQueryParams() {
        HttpCallParser parser = new HttpCallParser("GET /users?name=john&age=30");
        
        assertEquals("GET", parser.getMethod());
        assertEquals("/users", parser.getPath());
        
        Map<String, String> params = parser.getQueryParams();
        assertEquals(2, params.size());
        assertEquals("john", params.get("name"));
        assertEquals("30", params.get("age"));
    }
    
    @Test
    void testParseUrlEncodedQueryParams() {
        HttpCallParser parser = new HttpCallParser("GET policies?$select=code&$filter=code%20eq%20'excludeLate'&$skip=0&$top=100");
        
        assertEquals("GET", parser.getMethod());
        assertEquals("/policies", parser.getPath());
        
        Map<String, String> params = parser.getQueryParams();
        assertEquals(4, params.size());
        assertEquals("code", params.get("$select"));
        assertEquals("code eq 'excludeLate'", params.get("$filter"));
        assertEquals("0", params.get("$skip"));
        assertEquals("100", params.get("$top"));
    }
    
    @Test
    void testParsePostRequest() {
        HttpCallParser parser = new HttpCallParser("POST /api/users?format=json");
        
        assertEquals("POST", parser.getMethod());
        assertEquals("/api/users", parser.getPath());
        assertEquals("json", parser.getQueryParams().get("format"));
    }
    
    @Test
    void testParsePutRequest() {
        HttpCallParser parser = new HttpCallParser("PUT /api/users/123");
        
        assertEquals("PUT", parser.getMethod());
        assertEquals("/api/users/123", parser.getPath());
    }
    
    @Test
    void testParseDeleteRequest() {
        HttpCallParser parser = new HttpCallParser("DELETE /api/users/123");
        
        assertEquals("DELETE", parser.getMethod());
        assertEquals("/api/users/123", parser.getPath());
    }
    
    @Test
    void testParseLowercaseMethod() {
        HttpCallParser parser = new HttpCallParser("get /users");
        
        assertEquals("GET", parser.getMethod());
    }
    
    @Test
    void testParseMixedCaseMethod() {
        HttpCallParser parser = new HttpCallParser("GeT /users");
        
        assertEquals("GET", parser.getMethod());
    }
    
    @Test
    void testParsePathWithoutLeadingSlash() {
        HttpCallParser parser = new HttpCallParser("GET users");
        
        assertEquals("/users", parser.getPath());
    }
    
    @Test
    void testParseEmptyQueryParamValue() {
        HttpCallParser parser = new HttpCallParser("GET /search?q=");
        
        Map<String, String> params = parser.getQueryParams();
        assertEquals("", params.get("q"));
    }
    
    @Test
    void testParseQueryParamWithoutValue() {
        HttpCallParser parser = new HttpCallParser("GET /search?debug");
        
        Map<String, String> params = parser.getQueryParams();
        assertEquals("", params.get("debug"));
    }
    
    @Test
    void testParseMultipleQueryParamsWithSpecialChars() {
        HttpCallParser parser = new HttpCallParser("GET /search?q=hello%20world&filter=type%3Dbook");
        
        Map<String, String> params = parser.getQueryParams();
        assertEquals("hello world", params.get("q"));
        assertEquals("type=book", params.get("filter"));
    }
    
    @Test
    void testParseQueryParamsPreservesOrder() {
        HttpCallParser parser = new HttpCallParser("GET /api?z=1&a=2&m=3");
        
        Map<String, String> params = parser.getQueryParams();
        String[] keys = params.keySet().toArray(new String[0]);
        assertEquals("z", keys[0]);
        assertEquals("a", keys[1]);
        assertEquals("m", keys[2]);
    }
    
    @Test
    void testParseComplexODataQuery() {
        HttpCallParser parser = new HttpCallParser("GET /odata/Products?$filter=Price%20gt%2020%20and%20Price%20lt%20100&$orderby=Name&$top=10");
        
        assertEquals("GET", parser.getMethod());
        assertEquals("/odata/Products", parser.getPath());
        
        Map<String, String> params = parser.getQueryParams();
        assertEquals("Price gt 20 and Price lt 100", params.get("$filter"));
        assertEquals("Name", params.get("$orderby"));
        assertEquals("10", params.get("$top"));
    }
    
    @Test
    void testNullInputThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpCallParser(null);
        });
    }
    
    @Test
    void testEmptyInputThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpCallParser("");
        });
    }
    
    @Test
    void testWhitespaceOnlyInputThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpCallParser("   ");
        });
    }
    
    @Test
    void testInvalidFormatThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpCallParser("GETUSERS");
        });
    }
    
    @Test
    void testParseWithExtraWhitespace() {
        HttpCallParser parser = new HttpCallParser("  GET   /users?name=john  ");
        
        assertEquals("GET", parser.getMethod());
        assertEquals("/users", parser.getPath());
        assertEquals("john", parser.getQueryParams().get("name"));
    }
    
    @Test
    void testParseQueryParamWithPlusSign() {
        HttpCallParser parser = new HttpCallParser("GET /search?q=hello+world");
        
        assertEquals("hello world", parser.getQueryParams().get("q"));
    }
    
    @Test
    void testParseQueryParamWithEncodedSpecialChars() {
        HttpCallParser parser = new HttpCallParser("GET /api?email=user%40example.com&path=%2Fhome%2Fuser");
        
        Map<String, String> params = parser.getQueryParams();
        assertEquals("user@example.com", params.get("email"));
        assertEquals("/home/user", params.get("path"));
    }
}
