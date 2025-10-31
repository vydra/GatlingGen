package ggen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CLI {
    public String generateGatlingCode(String httpCall) {
        HttpCallParser parser = new HttpCallParser(httpCall);
        GatlingCodeGenerator generator = new GatlingCodeGenerator();
        
        return generator.generate(
            parser.getMethod(),
            parser.getPath(),
            parser.getQueryParams()
        );
    }

    public String generateGatlingCode(String httpCall, String targetDirectory) {
        HttpCallParser parser = new HttpCallParser(httpCall);
        GatlingCodeGenerator generator = new GatlingCodeGenerator(targetDirectory);
        
        return generator.generate(
            parser.getMethod(),
            parser.getPath(),
            parser.getQueryParams()
        );
    }

    public String generateGatlingCodeFromFile(String filename) throws IOException {
        Path filePath = Paths.get(filename);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filename);
        }
        
        if (!Files.isReadable(filePath)) {
            throw new IOException("File is not readable: " + filename);
        }
        
        String httpCall = Files.readString(filePath).trim();
        
        if (httpCall.isEmpty()) {
            throw new IllegalArgumentException("HTTP file is empty: " + filename);
        }
        
        String targetDirectory = filePath.getParent() != null ? filePath.getParent().toString() : ".";
        return generateGatlingCode(httpCall, targetDirectory);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: ggen <filename.http>");
            System.err.println("Example: ggen get_policies.http");
            System.err.println("The HTTP file should contain a line like: GET policies?$select=code&$filter=code%20eq%20'excludeLate'&$skip=0&$top=100");
            System.exit(1);
        }

        try {
            CLI cli = new CLI();
            String filename = args[0];
            
            String generatedCode = cli.generateGatlingCodeFromFile(filename);
            System.out.println(generatedCode);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
