package ggen;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

class GatlingCodeGeneratorTest {
    
    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }
    
    @Test
    void testGenerateSimpleGetRequest() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        String result = generator.generate("GET", "/users", params);
        
        assertTrue(result.contains("http(\"name of this step\")"));
        assertTrue(result.contains(".get(\"/users\")"));
        assertTrue(result.contains(".disableUrlEncoding()"));
        assertTrue(result.contains(".check(status().is(200))"));
    }
    
    @Test
    void testGenerateGetRequestWithQueryParams() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("name", "john");
        params.put("age", "30");
        
        String result = generator.generate("GET", "/users", params);
        
        assertTrue(result.contains(".queryParam(\"name\",\"john\")"));
        assertTrue(result.contains(".queryParam(\"age\",\"30\")"));
    }
    
    @Test
    void testGenerateWithODataFilter() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("$select", "code");
        params.put("$filter", "code eq 'excludeLate'");
        params.put("$skip", "0");
        params.put("$top", "100");
        
        String result = generator.generate("GET", "/policies", params);
        
        assertTrue(result.contains(".queryParam(\"$select\",\"code\")"));
        assertTrue(result.contains(".queryParam(\"$filter\",TXNUtils.encodeForOdata(\"code eq 'excludeLate'\"))"));
        assertTrue(result.contains(".queryParam(\"$skip\",\"0\")"));
        assertTrue(result.contains(".queryParam(\"$top\",\"100\")"));
    }
    
    @Test
    void testGenerateCompleteExampleFromSpec() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("$select", "code");
        params.put("$filter", "code eq 'excludeLate'");
        params.put("$skip", "0");
        params.put("$top", "100");
        
        String result = generator.generate("GET", "/policies", params);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testGeneratePostRequest() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        String result = generator.generate("POST", "/api/users", params);
        
        assertTrue(result.contains(".post(\"/api/users\")"));
    }
    
    @Test
    void testGeneratePutRequest() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        String result = generator.generate("PUT", "/api/users/123", params);
        
        assertTrue(result.contains(".put(\"/api/users/123\")"));
    }
    
    @Test
    void testGenerateDeleteRequest() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        String result = generator.generate("DELETE", "/api/users/123", params);
        
        assertTrue(result.contains(".delete(\"/api/users/123\")"));
    }
    
    @Test
    void testGeneratePatchRequest() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        String result = generator.generate("PATCH", "/api/users/123", params);
        
        assertTrue(result.contains(".patch(\"/api/users/123\")"));
    }
    
    @Test
    void testGenerateHeadRequest() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        String result = generator.generate("HEAD", "/api/status", params);
        
        assertTrue(result.contains(".head(\"/api/status\")"));
    }
    
    @Test
    void testGenerateOptionsRequest() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        String result = generator.generate("OPTIONS", "/api", params);
        
        assertTrue(result.contains(".options(\"/api\")"));
    }
    
    @Test
    void testODataFilterDetectionWithEq() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("$filter", "name eq 'John'");
        
        String result = generator.generate("GET", "/users", params);
        
        assertTrue(result.contains("TXNUtils.encodeForOdata(\"name eq 'John'\")"));
    }
    
    @Test
    void testODataFilterDetectionWithNe() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("filter", "status ne 'inactive'");
        
        String result = generator.generate("GET", "/users", params);
        
        assertTrue(result.contains("TXNUtils.encodeForOdata(\"status ne 'inactive'\")"));
    }
    
    @Test
    void testODataFilterDetectionWithGt() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("filter", "age gt 18");
        
        String result = generator.generate("GET", "/users", params);
        
        assertTrue(result.contains("TXNUtils.encodeForOdata(\"age gt 18\")"));
    }
    
    @Test
    void testODataFilterDetectionWithLt() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("filter", "price lt 100");
        
        String result = generator.generate("GET", "/products", params);
        
        assertTrue(result.contains("TXNUtils.encodeForOdata(\"price lt 100\")"));
    }
    
    @Test
    void testODataFilterDetectionWithGe() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("filter", "age ge 21");
        
        String result = generator.generate("GET", "/users", params);
        
        assertTrue(result.contains("TXNUtils.encodeForOdata(\"age ge 21\")"));
    }
    
    @Test
    void testODataFilterDetectionWithLe() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("filter", "price le 50");
        
        String result = generator.generate("GET", "/products", params);
        
        assertTrue(result.contains("TXNUtils.encodeForOdata(\"price le 50\")"));
    }
    
    @Test
    void testNonODataFilterNotWrapped() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("filter", "active");
        params.put("search", "test");
        
        String result = generator.generate("GET", "/users", params);
        
        assertTrue(result.contains(".queryParam(\"filter\",\"active\")"));
        assertTrue(result.contains(".queryParam(\"search\",\"test\")"));
        assertFalse(result.contains("TXNUtils.encodeForOdata(\"active\")"));
        assertFalse(result.contains("TXNUtils.encodeForOdata(\"test\")"));
    }
    
    @Test
    void testQueryParamsPreserveOrder() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("z", "1");
        params.put("a", "2");
        params.put("m", "3");
        
        String result = generator.generate("GET", "/api", params);
        
        int zIndex = result.indexOf(".queryParam(\"z\"");
        int aIndex = result.indexOf(".queryParam(\"a\"");
        int mIndex = result.indexOf(".queryParam(\"m\"");
        
        assertTrue(zIndex < aIndex);
        assertTrue(aIndex < mIndex);
    }
    
    @Test
    void testGenerateWithEmptyQueryParamValue() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("debug", "");
        
        String result = generator.generate("GET", "/api", params);
        
        assertTrue(result.contains(".queryParam(\"debug\",\"\")"));
    }
    
    @Test
    void testGenerateWithNullParams() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        
        String result = generator.generate("GET", "/users", null);
        
        assertTrue(result.contains("http(\"name of this step\")"));
        assertTrue(result.contains(".get(\"/users\")"));
        assertTrue(result.contains(".disableUrlEncoding()"));
        assertTrue(result.contains(".check(status().is(200))"));
        assertFalse(result.contains(".queryParam"));
    }
    
    @Test
    void testNullMethodThrowsException() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(null, "/users", params);
        });
    }
    
    @Test
    void testEmptyMethodThrowsException() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("", "/users", params);
        });
    }
    
    @Test
    void testNullPathThrowsException() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("GET", null, params);
        });
    }
    
    @Test
    void testEmptyPathThrowsException() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("GET", "", params);
        });
    }
    
    @Test
    void testIndentationIsCorrect() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("test", "value");
        
        String result = generator.generate("GET", "/api", params);
        
        Approvals.verify(normalizeLineEndings(result));
    }
    
    @Test
    void testComplexODataFilterWithMultipleConditions() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("$filter", "Price gt 20 and Price lt 100");
        
        String result = generator.generate("GET", "/products", params);
        
        assertTrue(result.contains("TXNUtils.encodeForOdata(\"Price gt 20 and Price lt 100\")"));
    }
    
    @Test
    void testCaseInsensitiveODataOperators() {
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("filter", "name EQ 'test'");
        
        String result = generator.generate("GET", "/users", params);
        
        assertTrue(result.contains("TXNUtils.encodeForOdata(\"name EQ 'test'\")"));
    }
}
