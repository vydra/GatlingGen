# CLI

## Inputs

- HTTP_Method : String 
  - Example: `GET policies?$select=code&$filter=code%20eq%20'excludeLate'&$skip=0&$top=100`

## Output

Java code. Gatling simulation for the input HTTP call.

```
http("name of this step")
    .get("/policies")
    .disableUrlEncoding()
    .queryParam("$select","code")
    .queryParam("$filter",TXNUtils.encodeForOdata("code eq 'excludeLate'"))
    .queryParam("$skip", "0")
    .queryParam("$top", "100")
.check(status().is(200))
```

