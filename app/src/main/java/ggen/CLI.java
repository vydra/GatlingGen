package ggen;

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

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: ggen \"HTTP_METHOD path?params\"");
            System.err.println("Example: ggen \"GET policies?$select=code&$filter=code%20eq%20'excludeLate'&$skip=0&$top=100\"");
            System.exit(1);
        }

        try {
            CLI cli = new CLI();
            String httpCall;
            
            if (args.length == 1) {
                httpCall = args[0];
            } else {
                httpCall = String.join(" ", args);
            }
            
            String generatedCode = cli.generateGatlingCode(httpCall);
            System.out.println(generatedCode);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
