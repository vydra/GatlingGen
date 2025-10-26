package ggen;

import java.util.Map;

public class GatlingCodeGenerator {
    private static final String INDENT = "    ";

    public String generate(String method, String path, Map<String, String> queryParams) {
        if (method == null || method.isEmpty()) {
            throw new IllegalArgumentException("HTTP method cannot be null or empty");
        }
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        StringBuilder code = new StringBuilder();
        
        code.append("http(\"name of this step\")\n");
        code.append(INDENT).append(".").append(getGatlingMethod(method)).append("(\"").append(path).append("\")\n");
        code.append(INDENT).append(".disableUrlEncoding()\n");
        
        if (queryParams != null && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();
                
                code.append(INDENT).append(".queryParam(\"").append(paramName).append("\",");
                
                if (isODataFilter(paramName, paramValue)) {
                    code.append("TXNUtils.encodeForOdata(\"").append(paramValue).append("\")");
                } else {
                    code.append("\"").append(paramValue).append("\"");
                }
                
                code.append(")\n");
            }
        }
        
        code.append(".check(status().is(200))");
        
        return code.toString();
    }

    private String getGatlingMethod(String httpMethod) {
        switch (httpMethod.toUpperCase()) {
            case "GET":
                return "get";
            case "POST":
                return "post";
            case "PUT":
                return "put";
            case "DELETE":
                return "delete";
            case "PATCH":
                return "patch";
            case "HEAD":
                return "head";
            case "OPTIONS":
                return "options";
            default:
                return "get";
        }
    }

    private boolean isODataFilter(String paramName, String paramValue) {
        if (paramName == null || paramValue == null) {
            return false;
        }
        
        if (paramName.equals("$filter")) {
            return true;
        }
        
        String lowerValue = paramValue.toLowerCase();
        return lowerValue.contains(" eq ") || 
               lowerValue.contains(" ne ") || 
               lowerValue.contains(" gt ") || 
               lowerValue.contains(" lt ") || 
               lowerValue.contains(" ge ") || 
               lowerValue.contains(" le ");
    }
}
