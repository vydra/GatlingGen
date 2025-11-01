# CLI

## Inputs

- HTTP file name
- Example file: app/src/test/resources/get_policies.http
 
## Output

Java code. Gatling simulation for the input HTTP call.

```
http("name of this step")
    .get("/policies")
    .disableUrlEncoding()
    .queryParam("$select","code")
    .queryParam("$filter",Utils.encodeForOdata("code eq 'excludeLate'"))
    .queryParam("$skip", "0")
    .queryParam("$top", "100")
.check(status().is(200))
```

