package ggen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class GatlingCodeGenerator {
    private static final String INDENT = "    ";
    private static final String DEFAULT_ENCODE_FUNCTION = "TXNUtils.encodeForOdata";
    private final String encodeFunction;

    public GatlingCodeGenerator() {
        this.encodeFunction = DEFAULT_ENCODE_FUNCTION;
    }

    public GatlingCodeGenerator(String targetDirectory) {
        this.encodeFunction = readEncodeFunctionFromConfig(targetDirectory);
    }

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
                    code.append(encodeFunction).append("(\"").append(paramValue).append("\")");
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
        return switch (httpMethod.toUpperCase()) {
            case "GET" -> "get";
            case "POST" -> "post";
            case "PUT" -> "put";
            case "DELETE" -> "delete";
            case "PATCH" -> "patch";
            case "HEAD" -> "head";
            case "OPTIONS" -> "options";
            default -> "get";
        };
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

    private String readEncodeFunctionFromConfig(String targetDirectory) {
        if (targetDirectory == null || targetDirectory.isEmpty()) {
            return DEFAULT_ENCODE_FUNCTION;
        }

        Path configPath = Paths.get(targetDirectory, ".ggen");
        
        if (!Files.exists(configPath)) {
            return DEFAULT_ENCODE_FUNCTION;
        }

        try {
            String configContent = Files.readString(configPath).trim();
            if (configContent.startsWith("encodeFunction=")) {
                return configContent.substring("encodeFunction=".length()).trim();
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not read config file " + configPath + ": " + e.getMessage());
        }
        // If we can't read the config, use default
        return DEFAULT_ENCODE_FUNCTION;
    }
}
