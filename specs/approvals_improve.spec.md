# Goals

Show approvals expected text as a comment in the unit test.

## Happy Path

Given junit test testGenerateGatlingCodeWithSpecExample
AND approvals file named 'GatlingCodeGeneratorTest.testGenerateCompleteExampleFromSpec.approved.txt'
AND the approval file contains:

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

THEN

Rewrite the CLITest.java and add the contents of the approvals fils as a block Java comment as below:

```
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

## Negative Paths

1. **Approval File Missing**
    - Given junit test `testGenerateGatlingCodeWithSpecExample`
    - AND no approvals file exists with the name `GatlingCodeGeneratorTest.testGenerateCompleteExampleFromSpec.approved.txt`
    - THEN the tool should warn with a clear error message: "Approval file not found for testGenerateGatlingCodeWithSpecExample."

2. **Approval File Empty**
    - Given junit test `testGenerateGatlingCodeWithSpecExample`
    - AND the approvals file `GatlingCodeGeneratorTest.testGenerateCompleteExampleFromSpec.approved.txt` is empty
    - THEN the tool should fail with a clear error message: "Approval file GatlingCodeGeneratorTest.testGenerateCompleteExampleFromSpec.approved.txt is empty."
