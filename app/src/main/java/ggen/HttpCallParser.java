package ggen;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class HttpCallParser {
    private String method;
    private String path;
    private LinkedHashMap<String, String> queryParams;

    public HttpCallParser(String httpCall) {
        if (httpCall == null || httpCall.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP call cannot be null or empty");
        }

        parseHttpCall(httpCall.trim());
    }

    private void parseHttpCall(String httpCall) {
        String[] parts = httpCall.split("\\s+", 2);
        
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid HTTP call format. Expected: 'METHOD path?params'");
        }

        this.method = parts[0].toUpperCase();
        String urlPart = parts[1];

        int queryIndex = urlPart.indexOf('?');
        if (queryIndex == -1) {
            this.path = urlPart;
            this.queryParams = new LinkedHashMap<>();
        } else {
            this.path = urlPart.substring(0, queryIndex);
            String queryString = urlPart.substring(queryIndex + 1);
            this.queryParams = parseQueryString(queryString);
        }

        if (!this.path.startsWith("/")) {
            this.path = "/" + this.path;
        }
    }

    private LinkedHashMap<String, String> parseQueryString(String queryString) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = urlDecode(pair.substring(0, idx));
                String value = urlDecode(pair.substring(idx + 1));
                params.put(key, value);
            } else if (idx == 0) {
                continue;
            } else {
                String key = urlDecode(pair);
                params.put(key, "");
            }
        }

        return params;
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public LinkedHashMap<String, String> getQueryParams() {
        return new LinkedHashMap<>(queryParams);
    }
}
