# Configuration

Configuration file named .ggen in the target directory can be used to set default options for GatlingGen CLI.

Target directory environment variable is GGEN_TARGET_DIR

## Example .ggen file

```encodeFunction=Utils.encode
```

## Scenario: substitute function name

GIVEN:
FIle .ggen with content:
```encodeFunction=Utils.encode
```
AND api call: `GET policies?$select=code&$filter=code%20eq%20'excludeLate'&$skip=0&$top=100`

WHEN:

ggen is executed

THEN:

Generated code uses Utils.encode function as below:

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